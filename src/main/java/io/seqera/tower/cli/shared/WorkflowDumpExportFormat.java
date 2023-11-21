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

package io.seqera.tower.cli.shared;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.runs.DumpCmd;
import io.seqera.tower.model.Workflow;

import java.io.IOException;
import java.util.List;

/**
 * This is the class used by {@link DumpCmd} to structure Workflow data as JSON.
 * The class {@link Workflow} is missing some extra fields so this envelope
 * class is needed to export/import them along the rest of the data.
 */
public final class WorkflowDumpExportFormat {

    private Workflow workflow;

    private Long pipelineId;

    private Long workspaceId;

    private Long userId;

    private String userEmail;

    public WorkflowDumpExportFormat(
            final Workflow workflow,
            final Long pipelineId,
            final Long workspaceId,
            final Long userId,
            final String userEmail
    ) {
        this.workflow = workflow;
        this.pipelineId = pipelineId;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public static WorkflowDumpExportFormat deserialize(final String json) throws JsonProcessingException {
        return buildMapper()
                .registerModule(
                        new SimpleModule()
                                .addDeserializer(WorkflowDumpExportFormat.class, new WorkflowDumpExportFormatDeserializer())
                )
                .readValue(json, WorkflowDumpExportFormat.class);
    }

    /**
     * This mixin class allows to add a virtual fields to {@link Workflow}.
     * See attribute inclusion in {@link WorkflowDumpExportFormat#serialize(WorkflowDumpExportFormat)}
     */
    @JsonAppend(attrs = {
            @JsonAppend.Attr(value = "pipelineId"),
            @JsonAppend.Attr(value = "workspaceId"),
            @JsonAppend.Attr(value = "userId"),
            @JsonAppend.Attr(value = "userEmail"),
    })
    public static abstract class WorkflowDumpMixin {}

    public static String serialize(final WorkflowDumpExportFormat wfExport) throws JsonProcessingException {
        return buildMapper()
                .writerWithDefaultPrettyPrinter()
                .withAttribute("pipelineId", wfExport.getPipelineId())
                .withAttribute("workspaceId", wfExport.getWorkspaceId())
                .withAttribute("userId", wfExport.getUserId())
                .withAttribute("userEmail", wfExport.getUserEmail())
                .writeValueAsString(wfExport.getWorkflow());
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new JSON().getContext(Workflow.class)
                .addMixIn(Workflow.class, WorkflowDumpMixin.class);

        mapper.configOverride(List.class).setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));

        return mapper;
    }

    /**
     * Custom deserializer for extended {@link Workflow} JSON representation including virtual fields.
     * Handles missing virtual fields.
     */
    public static class WorkflowDumpExportFormatDeserializer extends StdDeserializer<WorkflowDumpExportFormat> {

        public WorkflowDumpExportFormatDeserializer() {
            this(null);
        }

        public WorkflowDumpExportFormatDeserializer(Class<WorkflowDumpExportFormat> vc) {
            super(vc);
        }

        @Override
        public WorkflowDumpExportFormat deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {

            Workflow workflow = null;
            Long pipelineId = null;
            Long workspaceId = null;
            Long userId = null;
            String userEmail = null;

            ObjectMapper mapper = buildMapper()
                    // virtual fields not mandatory
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonNode root = parser.getCodec().readTree(parser);

            workflow = mapper.readValue(root.toString(), Workflow.class);

            JsonNode pipelineIdNode = root.get("pipelineId");
            if (pipelineIdNode != null) {
                pipelineId = mapper.readValue(pipelineIdNode.toString(), new TypeReference<Long>() {});
            }

            JsonNode workspaceIdNode = root.get("workspaceId");
            if (workspaceIdNode != null) {
                workspaceId = mapper.readValue(workspaceIdNode.toString(), new TypeReference<Long>() {});
            }

            JsonNode userIdNode = root.get("userId");
            if (userIdNode != null) {
                userId = mapper.readValue(userIdNode.toString(), new TypeReference<Long>() {});
            }

            JsonNode userEmailNode = root.get("userEmail");
            if (userEmailNode != null) {
                userEmail = mapper.readValue(userEmailNode.toString(), new TypeReference<String>() {});
            }

            return new WorkflowDumpExportFormat(
                    workflow,
                    pipelineId,
                    workspaceId,
                    userId,
                    userEmail
            );
        }
    }
}
