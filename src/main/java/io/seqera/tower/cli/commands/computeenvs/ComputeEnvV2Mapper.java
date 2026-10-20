/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.computeenvs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.ComputeEnvStatus;
import io.seqera.tower.model.LabelDbDto;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.v2.model.ComputeEnvConfigWriteModel;
import io.seqera.tower.v2.model.ComputeEnvReadModel;
import io.seqera.tower.v2.model.ComputeEnvResourceLabelReadModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates between the CLI's compute-environment model (the v1 SDK types, which also define the
 * export/import file format and the per-platform option builders) and the Platform API v2 contract.
 * Translation only: no defaults, validations or business rules live here.
 */
final class ComputeEnvV2Mapper {

    // The v1 SDK names a config variant by its `discriminator`; the v2 contract by its `platform`.
    private static final Map<String, String> V1_TO_V2_PLATFORM = Map.ofEntries(
            Map.entry("aws-batch", "AWS_BATCH"),
            Map.entry("aws-cloud", "AWS_CLOUD"),
            Map.entry("seqeracompute-platform", "SEQERA_COMPUTE"),
            Map.entry("google-batch", "GOOGLE_BATCH"),
            Map.entry("google-cloud", "GOOGLE_CLOUD"),
            Map.entry("google-lifesciences", "GOOGLE_LIFE_SCIENCES"),
            Map.entry("azure-batch", "AZURE_BATCH"),
            Map.entry("azure-cloud", "AZURE_CLOUD"),
            Map.entry("k8s-platform", "K8S"),
            Map.entry("eks-platform", "EKS"),
            Map.entry("gke-platform", "GKE"),
            Map.entry("uge-platform", "UGE"),
            Map.entry("slurm-platform", "SLURM"),
            Map.entry("lsf-platform", "LSF"),
            Map.entry("altair-platform", "ALTAIR_PBS"),
            Map.entry("moab-platform", "MOAB"),
            Map.entry("local-platform", "LOCAL")
    );

    private static final Map<String, String> V2_TO_V1_PLATFORM = V1_TO_V2_PLATFORM.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    // Field names spelled differently by the two contracts (v1 name -> v2 name), at any depth of the config.
    private static final Map<String, String> V1_TO_V2_FIELD = Map.of(
            "nvnmeStorageEnabled", "nvmeStorageEnabled",
            "bootDiskSizeGB", "bootDiskSizeGb"
    );

    private static final Map<String, String> V2_TO_V1_FIELD = V1_TO_V2_FIELD.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    // Enum values spelled differently by the two contracts, per field name (v1 value -> v2 value).
    private static final Map<String, Map<String, String>> V1_TO_V2_VALUE = Map.of(
            "podCleanup", Map.of("on_success", "ON_SUCCESS", "always", "ALWAYS", "never", "NEVER"),
            "provisioningModel", Map.of("spot", "SPOT", "spotFirst", "SPOT_FIRST", "ondemand", "ON_DEMAND")
    );

    private static final Map<String, Map<String, String>> V2_TO_V1_VALUE = V1_TO_V2_VALUE.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey))));

    // Server-populated v1 fields an exported config may carry; the server never read them on create.
    private static final List<String> V1_SERVER_POPULATED = List.of("forgedResources", "deletedResources");

    private ComputeEnvV2Mapper() {
    }

    private static ObjectMapper v1Mapper() {
        return new io.seqera.tower.JSON().getContext(ComputeConfig.class)
                .copy()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static ObjectMapper v2Mapper() {
        return new io.seqera.tower.v2.JSON().getMapper().copy()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    static ComputeEnvConfigWriteModel toV2Config(ComputeConfig config) {
        ObjectNode tree = v1Mapper().valueToTree(config);
        String discriminator = tree.path("discriminator").asText(null);
        String platform = V1_TO_V2_PLATFORM.get(discriminator);
        if (platform == null) {
            throw new TowerRuntimeException(String.format("Unknown compute environment platform '%s'", discriminator));
        }
        tree.remove("discriminator");
        tree.remove(V1_SERVER_POPULATED);
        rename(tree, V1_TO_V2_FIELD, V1_TO_V2_VALUE);
        tree.put("platform", platform);

        // KEPT: no server equivalent (tower-cli:rule-21) - added for v2: fail loudly rather than silently drop a setting
        // the v2 contract cannot carry (e.g. Google Cloud zone, Azure Cloud resource group, AWS Cloud subnetId).
        ObjectMapper strict = v2Mapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        try {
            return strict.treeToValue(tree, ComputeEnvConfigWriteModel.class);
        } catch (UnrecognizedPropertyException e) {
            throw new TowerRuntimeException(String.format("The '%s' setting of a %s compute environment is not supported by the Platform API v2", e.getPropertyName(), platform));
        } catch (JsonProcessingException e) {
            throw new TowerRuntimeException(String.format("Unable to build the %s compute environment request: %s", platform, e.getOriginalMessage()));
        }
    }

    static ComputeEnvResponseDto toV1(ComputeEnvReadModel ce, Long workspaceId) {
        ComputeConfig config = toV1Config(ce);
        return new ComputeEnvResponseDto()
                .id(ce.getId())
                .name(ce.getName())
                .description(ce.getDescription())
                .platform(config == null ? null : ComputeEnvResponseDto.PlatformEnum.fromValue(config.getDiscriminator()))
                .config(config)
                .status(toV1(ce.getStatus()))
                .message(ce.getMessage())
                .primary(ce.getPrimary())
                .credentialsId(ce.getCredentialsId())
                .managedIdentityId(ce.getManagedIdentityId())
                .fusionMetricsCollectionEnabled(ce.getFusionMetricsCollectionEnabled())
                .dateCreated(ce.getCreatedAt())
                .lastUpdated(ce.getUpdatedAt())
                .lastUsed(ce.getLastUsedAt())
                .labels(toV1Labels(ce.getResourceLabels()))
                .workspaceId(workspaceId);
    }

    static ListComputeEnvsResponseEntry toV1ListEntry(ComputeEnvReadModel ce) {
        ComputeConfig config = toV1Config(ce);
        return new ListComputeEnvsResponseEntry()
                .id(ce.getId())
                .name(ce.getName())
                .description(ce.getDescription())
                .platform(config == null ? null : config.getDiscriminator())
                .status(toV1(ce.getStatus()))
                .message(ce.getMessage())
                .primary(ce.getPrimary())
                .credentialsId(ce.getCredentialsId())
                .lastUsed(ce.getLastUsedAt())
                .workDir(config == null ? null : config.getWorkDir())
                .labels(toV1Labels(ce.getResourceLabels()));
    }

    static ComputeEnvStatus toV1(io.seqera.tower.v2.model.ComputeEnvStatus status) {
        return status == null ? null : ComputeEnvStatus.fromValue(status.getValue());
    }

    private static ComputeConfig toV1Config(ComputeEnvReadModel ce) {
        if (ce.getConfig() == null || ce.getConfig().getActualInstance() == null) {
            return null;
        }
        ObjectNode tree = v2Mapper().valueToTree(ce.getConfig());
        String platform = tree.path("platform").asText(null);
        tree.remove("platform");
        rename(tree, V2_TO_V1_FIELD, V2_TO_V1_VALUE);
        tree.put("discriminator", V2_TO_V1_PLATFORM.getOrDefault(platform, platform));
        try {
            return v1Mapper().treeToValue(tree, ComputeConfig.class);
        } catch (JsonProcessingException e) {
            throw new TowerRuntimeException(String.format("Unable to read the %s compute environment configuration: %s", platform, e.getOriginalMessage()));
        }
    }

    private static List<LabelDbDto> toV1Labels(List<ComputeEnvResourceLabelReadModel> labels) {
        if (labels == null) {
            return null;
        }
        List<LabelDbDto> result = new ArrayList<>();
        for (ComputeEnvResourceLabelReadModel label : labels) {
            result.add(new LabelDbDto()
                    .id(Long.valueOf(label.getId()))
                    .name(label.getName())
                    .value(label.getValue())
                    .resource(true));
        }
        return result;
    }

    private static void rename(JsonNode node, Map<String, String> names, Map<String, Map<String, String>> values) {
        if (node instanceof ObjectNode) {
            ObjectNode obj = (ObjectNode) node;
            for (Map.Entry<String, String> name : names.entrySet()) {
                if (obj.has(name.getKey())) {
                    obj.set(name.getValue(), obj.remove(name.getKey()));
                }
            }
            for (Map.Entry<String, Map<String, String>> field : values.entrySet()) {
                JsonNode value = obj.get(field.getKey());
                if (value != null && value.isTextual() && field.getValue().containsKey(value.asText())) {
                    obj.put(field.getKey(), field.getValue().get(value.asText()));
                }
            }
            for (Iterator<JsonNode> it = obj.elements(); it.hasNext(); ) {
                rename(it.next(), names, values);
            }
        } else if (node instanceof ArrayNode) {
            for (JsonNode child : node) {
                rename(child, names, values);
            }
        }
    }
}
