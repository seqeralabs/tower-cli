/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.utils.metadata;

import io.seqera.tower.cli.Tower;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts CLI metadata from picocli annotations using reflection.
 *
 * This approach is deterministic and captures all command metadata including:
 * - Complete command hierarchy with resolved mixins
 * - All options with descriptions, defaults, arity, required status
 * - Positional parameters
 * - Hidden commands and options
 *
 * Usage:
 *   java -cp <classpath> io.seqera.tower.cli.utils.metadata.CliMetadataExtractor [output-dir]
 *
 * Outputs two files:
 *   - cli-metadata.json: Processed metadata with hierarchy and flat command map
 *   - command-spec.json: Raw picocli CommandSpec data without post-processing
 *
 * If no output directory is specified, writes cli-metadata.json to stdout.
 */
public class CliMetadataExtractor {

    private int totalCommands = 0;
    private int totalOptions = 0;
    private int totalParameters = 0;

    public static void main(String[] args) {
        CliMetadataExtractor extractor = new CliMetadataExtractor();

        // Build the command line once
        Tower app = new Tower();
        CommandLine cmd = new CommandLine(app);
        cmd.setUsageHelpLongOptionsMaxWidth(40);
        cmd.setCaseInsensitiveEnumValuesAllowed(true);
        CommandSpec rootSpec = cmd.getCommandSpec();

        if (args.length > 0) {
            String outputDir = args[0];
            // If it's a file path ending in .json, use its directory
            if (outputDir.endsWith(".json")) {
                outputDir = new java.io.File(outputDir).getParent();
                if (outputDir == null) outputDir = ".";
            }

            // Write processed metadata
            String metadataJson = extractor.extractMetadataAsJson(rootSpec);
            String metadataPath = outputDir + "/cli-metadata.json";
            try (PrintWriter writer = new PrintWriter(new FileWriter(metadataPath))) {
                writer.print(metadataJson);
                System.err.println("CLI metadata written to: " + metadataPath);
                System.err.println("Total commands: " + extractor.totalCommands);
                System.err.println("Total options: " + extractor.totalOptions);
                System.err.println("Total parameters: " + extractor.totalParameters);
            } catch (IOException e) {
                System.err.println("Error writing metadata file: " + e.getMessage());
                System.exit(1);
            }

            // Write raw command spec
            String rawJson = extractor.extractRawSpecAsJson(rootSpec);
            String rawPath = outputDir + "/command-spec.json";
            try (PrintWriter writer = new PrintWriter(new FileWriter(rawPath))) {
                writer.print(rawJson);
                System.err.println("Raw command spec written to: " + rawPath);
            } catch (IOException e) {
                System.err.println("Error writing raw spec file: " + e.getMessage());
                System.exit(1);
            }
        } else {
            String json = extractor.extractMetadataAsJson(rootSpec);
            System.out.print(json);
        }
    }

    /**
     * Extract all CLI metadata and return as JSON string.
     */
    public String extractMetadataAsJson(CommandSpec rootSpec) {
        Map<String, Object> output = new LinkedHashMap<>();

        // Metadata section
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("extractor_version", "2.0.0");
        metadata.put("extractor_type", "java-reflection");
        metadata.put("extracted_at", Instant.now().toString());
        output.put("metadata", metadata);

        // Extract command hierarchy
        Map<String, Object> hierarchy = extractCommand(rootSpec, null, "");
        output.put("hierarchy", hierarchy);

        // Flatten commands for easy lookup
        Map<String, Object> commands = new LinkedHashMap<>();
        flattenCommands(hierarchy, commands);
        output.put("commands", commands);

        // Update metadata with counts
        metadata.put("total_commands", totalCommands);
        metadata.put("total_options", totalOptions);
        metadata.put("total_parameters", totalParameters);

        return toJson(output, 0);
    }

    /**
     * Extract raw picocli CommandSpec data without post-processing.
     * This provides the unmodified picocli model for debugging and analysis.
     */
    public String extractRawSpecAsJson(CommandSpec rootSpec) {
        Map<String, Object> output = new LinkedHashMap<>();

        // Metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("extractor_version", "2.0.0");
        metadata.put("extractor_type", "java-reflection-raw");
        metadata.put("extracted_at", Instant.now().toString());
        output.put("metadata", metadata);

        // Raw command spec tree
        output.put("spec", extractRawCommand(rootSpec));

        return toJson(output, 0);
    }

    /**
     * Extract raw CommandSpec data recursively without transformation.
     */
    private Map<String, Object> extractRawCommand(CommandSpec spec) {
        Map<String, Object> cmd = new LinkedHashMap<>();

        // Basic info
        cmd.put("name", spec.name());
        cmd.put("aliases", Arrays.asList(spec.aliases()));
        cmd.put("qualifiedName", spec.qualifiedName());

        // Usage message details
        Map<String, Object> usageMessage = new LinkedHashMap<>();
        usageMessage.put("description", Arrays.asList(spec.usageMessage().description()));
        usageMessage.put("header", Arrays.asList(spec.usageMessage().header()));
        usageMessage.put("footer", Arrays.asList(spec.usageMessage().footer()));
        usageMessage.put("hidden", spec.usageMessage().hidden());
        usageMessage.put("synopsisHeading", spec.usageMessage().synopsisHeading());
        usageMessage.put("commandListHeading", spec.usageMessage().commandListHeading());
        usageMessage.put("requiredOptionMarker", String.valueOf(spec.usageMessage().requiredOptionMarker()));
        cmd.put("usageMessage", usageMessage);

        // User object class
        Object userObject = spec.userObject();
        if (userObject != null) {
            cmd.put("userObjectClass", userObject.getClass().getName());
        }

        // Version
        cmd.put("version", Arrays.asList(spec.version()));

        // All options (raw)
        List<Map<String, Object>> options = new ArrayList<>();
        for (OptionSpec opt : spec.options()) {
            options.add(extractRawOption(opt));
        }
        cmd.put("options", options);

        // Positional parameters (raw)
        List<Map<String, Object>> positionals = new ArrayList<>();
        for (PositionalParamSpec param : spec.positionalParameters()) {
            positionals.add(extractRawParameter(param));
        }
        cmd.put("positionalParameters", positionals);

        // Mixins (show what mixins were applied)
        Map<String, String> mixins = new LinkedHashMap<>();
        for (Map.Entry<String, CommandSpec> entry : spec.mixins().entrySet()) {
            Object mixinObj = entry.getValue().userObject();
            if (mixinObj != null) {
                mixins.put(entry.getKey(), mixinObj.getClass().getName());
            }
        }
        cmd.put("mixins", mixins);

        // Subcommands (recursive)
        Map<String, Object> subcommands = new LinkedHashMap<>();
        for (Map.Entry<String, CommandLine> entry : spec.subcommands().entrySet()) {
            String subName = entry.getKey();
            CommandSpec subSpec = entry.getValue().getCommandSpec();
            // Include all entries (including aliases)
            subcommands.put(subName, extractRawCommand(subSpec));
        }
        cmd.put("subcommands", subcommands);

        return cmd;
    }

    /**
     * Extract raw OptionSpec data.
     */
    private Map<String, Object> extractRawOption(OptionSpec opt) {
        Map<String, Object> option = new LinkedHashMap<>();

        option.put("names", Arrays.asList(opt.names()));
        option.put("description", Arrays.asList(opt.description()));
        option.put("required", opt.required());
        option.put("defaultValue", opt.defaultValue());
        option.put("arity", opt.arity().toString());
        option.put("arityMin", opt.arity().min());
        option.put("arityMax", opt.arity().max());
        option.put("hidden", opt.hidden());
        option.put("type", opt.type().getName());
        option.put("typeSimpleName", opt.type().getSimpleName());
        option.put("paramLabel", opt.paramLabel());
        option.put("splitRegex", opt.splitRegex());
        option.put("negatable", opt.negatable());
        option.put("interactive", opt.interactive());
        option.put("descriptionKey", opt.descriptionKey());
        option.put("order", opt.order());
        option.put("usageHelp", opt.usageHelp());
        option.put("versionHelp", opt.versionHelp());

        // Completions if available
        if (opt.completionCandidates() != null) {
            List<String> candidates = new ArrayList<>();
            opt.completionCandidates().forEach(c -> candidates.add(c.toString()));
            if (!candidates.isEmpty()) {
                option.put("completionCandidates", candidates);
            }
        }

        return option;
    }

    /**
     * Extract raw PositionalParamSpec data.
     */
    private Map<String, Object> extractRawParameter(PositionalParamSpec param) {
        Map<String, Object> parameter = new LinkedHashMap<>();

        parameter.put("index", param.index().toString());
        parameter.put("indexMin", param.index().min());
        parameter.put("indexMax", param.index().max());
        parameter.put("paramLabel", param.paramLabel());
        parameter.put("description", Arrays.asList(param.description()));
        parameter.put("arity", param.arity().toString());
        parameter.put("arityMin", param.arity().min());
        parameter.put("arityMax", param.arity().max());
        parameter.put("required", param.required());
        parameter.put("type", param.type().getName());
        parameter.put("typeSimpleName", param.type().getSimpleName());
        parameter.put("hidden", param.hidden());
        parameter.put("interactive", param.interactive());
        parameter.put("descriptionKey", param.descriptionKey());

        return parameter;
    }

    /**
     * Extract metadata from a CommandSpec recursively.
     */
    private Map<String, Object> extractCommand(CommandSpec spec, String parentName, String fullCommandPath) {
        Map<String, Object> cmd = new LinkedHashMap<>();

        String name = spec.name();
        String fullCommand = fullCommandPath.isEmpty() ? name : fullCommandPath + " " + name;

        totalCommands++;

        cmd.put("name", name);
        cmd.put("full_command", fullCommand);
        cmd.put("parent", parentName);

        // Description (may be multiline)
        String[] descArray = spec.usageMessage().description();
        String description = descArray.length > 0 ? String.join(" ", descArray) : null;
        cmd.put("description", description);

        // Hidden status
        cmd.put("hidden", spec.usageMessage().hidden());

        // Source class
        Object userObject = spec.userObject();
        if (userObject != null) {
            cmd.put("source_class", userObject.getClass().getName());
        }

        // Extract options (mixins are already resolved by picocli!)
        List<Map<String, Object>> options = new ArrayList<>();
        for (OptionSpec opt : spec.options()) {
            // Skip built-in help options from picocli
            if (isBuiltInOption(opt)) {
                continue;
            }
            options.add(extractOption(opt));
            totalOptions++;
        }
        cmd.put("options", options);

        // Extract positional parameters
        List<Map<String, Object>> parameters = new ArrayList<>();
        for (PositionalParamSpec param : spec.positionalParameters()) {
            parameters.add(extractParameter(param));
            totalParameters++;
        }
        cmd.put("parameters", parameters);

        // Extract subcommands recursively
        List<Map<String, Object>> children = new ArrayList<>();
        List<String> subcommandNames = new ArrayList<>();
        for (Map.Entry<String, CommandLine> entry : spec.subcommands().entrySet()) {
            String subName = entry.getKey();
            CommandSpec subSpec = entry.getValue().getCommandSpec();

            // Skip aliases (picocli registers aliases as separate entries)
            if (!subName.equals(subSpec.name())) {
                continue;
            }

            subcommandNames.add(subName);
            children.add(extractCommand(subSpec, name, fullCommand));
        }
        cmd.put("subcommands", subcommandNames);
        cmd.put("children", children);

        return cmd;
    }

    /**
     * Extract metadata from an OptionSpec.
     */
    private Map<String, Object> extractOption(OptionSpec opt) {
        Map<String, Object> option = new LinkedHashMap<>();

        // Names (e.g., ["-n", "--name"])
        List<String> names = new ArrayList<>();
        for (String name : opt.names()) {
            names.add(name);
        }
        option.put("names", names);

        // Description
        String[] descArray = opt.description();
        String description = descArray.length > 0 ? String.join(" ", descArray) : null;
        option.put("description", description);

        // Required
        option.put("required", opt.required());

        // Default value
        String defaultValue = opt.defaultValue();
        option.put("default_value", defaultValue);

        // Arity
        option.put("arity", opt.arity().toString());

        // Hidden
        option.put("hidden", opt.hidden());

        // Type information
        option.put("type", opt.type().getSimpleName());

        // Parameter label (shown in help)
        option.put("param_label", opt.paramLabel());

        // Split regex (for comma-separated values)
        String splitRegex = opt.splitRegex();
        if (splitRegex != null && !splitRegex.isEmpty()) {
            option.put("split", splitRegex);
        }

        // Negatable (for boolean options)
        option.put("negatable", opt.negatable());

        return option;
    }

    /**
     * Extract metadata from a PositionalParamSpec.
     */
    private Map<String, Object> extractParameter(PositionalParamSpec param) {
        Map<String, Object> parameter = new LinkedHashMap<>();

        // Index
        parameter.put("index", param.index().toString());

        // Parameter label
        parameter.put("param_label", param.paramLabel());

        // Description
        String[] descArray = param.description();
        String description = descArray.length > 0 ? String.join(" ", descArray) : null;
        parameter.put("description", description);

        // Arity
        parameter.put("arity", param.arity().toString());

        // Required (based on arity)
        parameter.put("required", param.arity().min() > 0);

        // Type information
        parameter.put("type", param.type().getSimpleName());

        // Hidden
        parameter.put("hidden", param.hidden());

        return parameter;
    }

    /**
     * Check if an option is a built-in picocli option (help, version).
     */
    private boolean isBuiltInOption(OptionSpec opt) {
        for (String name : opt.names()) {
            if (name.equals("-h") || name.equals("--help") ||
                name.equals("-V") || name.equals("--version")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Flatten the hierarchy into a map keyed by full command path.
     */
    @SuppressWarnings("unchecked")
    private void flattenCommands(Map<String, Object> node, Map<String, Object> result) {
        String fullCommand = (String) node.get("full_command");

        // Create a copy without children for the flat map
        Map<String, Object> flatNode = new LinkedHashMap<>(node);
        flatNode.remove("children");
        result.put(fullCommand, flatNode);

        // Recurse into children
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children != null) {
            for (Map<String, Object> child : children) {
                flattenCommands(child, result);
            }
        }
    }

    /**
     * Simple JSON serialization without external dependencies.
     * Produces well-formatted JSON output.
     */
    private String toJson(Object obj, int indent) {
        StringBuilder sb = new StringBuilder();
        toJson(obj, indent, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void toJson(Object obj, int indent, StringBuilder sb) {
        String indentStr = "  ".repeat(indent);
        String childIndent = "  ".repeat(indent + 1);

        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                sb.append(childIndent);
                sb.append("\"").append(escapeJson(entry.getKey())).append("\": ");
                toJson(entry.getValue(), indent + 1, sb);
                if (i < map.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
                i++;
            }
            sb.append(indentStr).append("}");
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if (list.isEmpty()) {
                sb.append("[]");
            } else if (list.get(0) instanceof String) {
                // Compact array for simple strings
                sb.append("[");
                for (int i = 0; i < list.size(); i++) {
                    sb.append("\"").append(escapeJson((String) list.get(i))).append("\"");
                    if (i < list.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            } else {
                sb.append("[\n");
                for (int i = 0; i < list.size(); i++) {
                    sb.append(childIndent);
                    toJson(list.get(i), indent + 1, sb);
                    if (i < list.size() - 1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                }
                sb.append(indentStr).append("]");
            }
        } else if (obj instanceof String) {
            sb.append("\"").append(escapeJson((String) obj)).append("\"");
        } else if (obj instanceof Boolean || obj instanceof Number) {
            sb.append(obj.toString());
        } else {
            sb.append("\"").append(escapeJson(obj.toString())).append("\"");
        }
    }

    /**
     * Escape special characters in JSON strings.
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
