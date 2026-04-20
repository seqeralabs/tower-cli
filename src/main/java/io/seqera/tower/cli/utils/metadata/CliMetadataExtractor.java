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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.seqera.tower.cli.Tower;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
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
 * Usage: java -cp <classpath> io.seqera.tower.cli.utils.metadata.CliMetadataExtractor [output-file]
 * If no output file is specified, the metadata is written to stdout.
 */
public class CliMetadataExtractor {
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writer();

    private int totalCommands = 0;
    private int totalOptions = 0;
    private int totalParameters = 0;

    public static void main(String[] args) throws IOException {
        CliMetadataExtractor extractor = new CliMetadataExtractor();
        String json = extractor.extractMetadataAsJson(buildRootSpec());

        if (args.length == 0) {
            System.out.print(json);
            return;
        }

        Path outputPath = resolveOutputPath(args[0]);
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(outputPath, json);
        System.err.println("CLI metadata written to: " + outputPath);
        System.err.println("Total commands: " + extractor.totalCommands);
        System.err.println("Total options: " + extractor.totalOptions);
        System.err.println("Total parameters: " + extractor.totalParameters);
    }

    /**
     * Build the picocli command tree used for metadata extraction.
     */
    static CommandSpec buildRootSpec() {
        CommandLine commandLine = new CommandLine(new Tower());
        commandLine.setUsageHelpLongOptionsMaxWidth(40);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        return commandLine.getCommandSpec();
    }

    private static Path resolveOutputPath(String outputArg) {
        Path outputPath = Path.of(outputArg);
        if (outputArg.endsWith(".json")) {
            return outputPath;
        }

        return outputPath.resolve("cli-metadata.json");
    }

    /**
     * Extract all CLI metadata and return it as a nested map structure.
     */
    public Map<String, Object> extractMetadata(CommandSpec rootSpec) {
        Map<String, Object> output = new LinkedHashMap<>();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("extractor_version", "2.0.0");
        metadata.put("extractor_type", "java-reflection");
        metadata.put("extracted_at", Instant.now().toString());
        output.put("metadata", metadata);

        Map<String, Object> hierarchy = extractCommand(rootSpec, null, "");
        output.put("hierarchy", hierarchy);

        Map<String, Object> commands = new LinkedHashMap<>();
        flattenCommands(hierarchy, commands);
        output.put("commands", commands);

        metadata.put("total_commands", totalCommands);
        metadata.put("total_options", totalOptions);
        metadata.put("total_parameters", totalParameters);

        return output;
    }

    /**
     * Extract all CLI metadata and return it as formatted JSON.
     */
    public String extractMetadataAsJson(CommandSpec rootSpec) throws JsonProcessingException {
        return JSON_WRITER.writeValueAsString(extractMetadata(rootSpec));
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

        String[] descArray = spec.usageMessage().description();
        String description = descArray.length > 0 ? String.join(" ", descArray) : null;
        cmd.put("description", description);

        cmd.put("hidden", spec.usageMessage().hidden());

        Object userObject = spec.userObject();
        if (userObject != null) {
            cmd.put("source_class", userObject.getClass().getName());
        }

        List<Map<String, Object>> options = new ArrayList<>();
        for (OptionSpec opt : spec.options()) {
            if (isBuiltInOption(opt)) {
                continue;
            }
            options.add(extractOption(opt));
            totalOptions++;
        }
        cmd.put("options", options);

        List<Map<String, Object>> parameters = new ArrayList<>();
        for (PositionalParamSpec param : spec.positionalParameters()) {
            parameters.add(extractParameter(param));
            totalParameters++;
        }
        cmd.put("parameters", parameters);

        List<Map<String, Object>> children = new ArrayList<>();
        List<String> subcommandNames = new ArrayList<>();
        for (Map.Entry<String, CommandLine> entry : spec.subcommands().entrySet()) {
            String subName = entry.getKey();
            CommandSpec subSpec = entry.getValue().getCommandSpec();

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

        List<String> names = new ArrayList<>();
        for (String name : opt.names()) {
            names.add(name);
        }
        option.put("names", names);

        String[] descArray = opt.description();
        String description = descArray.length > 0 ? String.join(" ", descArray) : null;
        option.put("description", description);

        option.put("required", opt.required());
        option.put("default_value", opt.defaultValue());
        option.put("arity", opt.arity().toString());
        option.put("hidden", opt.hidden());
        option.put("type", opt.type().getSimpleName());
        option.put("param_label", opt.paramLabel());

        String splitRegex = opt.splitRegex();
        if (splitRegex != null && !splitRegex.isEmpty()) {
            option.put("split", splitRegex);
        }

        option.put("negatable", opt.negatable());
        return option;
    }

    /**
     * Extract metadata from a PositionalParamSpec.
     */
    private Map<String, Object> extractParameter(PositionalParamSpec param) {
        Map<String, Object> parameter = new LinkedHashMap<>();
        parameter.put("index", param.index().toString());
        parameter.put("param_label", param.paramLabel());

        String[] descArray = param.description();
        String description = descArray.length > 0 ? String.join(" ", descArray) : null;
        parameter.put("description", description);

        parameter.put("arity", param.arity().toString());
        parameter.put("required", param.arity().min() > 0);
        parameter.put("type", param.type().getSimpleName());
        parameter.put("hidden", param.hidden());
        return parameter;
    }

    /**
     * Check if an option is a built-in picocli option (help, version).
     */
    private boolean isBuiltInOption(OptionSpec opt) {
        return opt.usageHelp() || opt.versionHelp();
    }

    /**
     * Flatten the hierarchy into a map keyed by full command path.
     */
    @SuppressWarnings("unchecked")
    private void flattenCommands(Map<String, Object> node, Map<String, Object> result) {
        String fullCommand = (String) node.get("full_command");

        Map<String, Object> flatNode = new LinkedHashMap<>(node);
        flatNode.remove("children");
        result.put(fullCommand, flatNode);

        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children != null) {
            for (Map<String, Object> child : children) {
                flattenCommands(child, result);
            }
        }
    }
}
