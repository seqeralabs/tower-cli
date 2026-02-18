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

package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.pipelines.versions.VersionRefOptions;
import io.seqera.tower.cli.exceptions.MultiplePipelinesFoundException;
import io.seqera.tower.cli.exceptions.PipelineNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.ListPipelineVersionsResponse;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineQueryAttribute;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Command
public abstract class AbstractPipelinesCmd extends AbstractApiCmd {

    public AbstractPipelinesCmd() {
    }

    protected PipelineDbDto pipelineByName(Long workspaceId, String pipelineName, List<PipelineQueryAttribute> pipelineQueryAttributes) throws ApiException {

        String exactName = quotePipelineName(pipelineName);

        ListPipelinesResponse list = pipelinesApi().listPipelines(pipelineQueryAttributes, workspaceId, null, null, null, null, exactName, "all");

        if (list.getPipelines().isEmpty()) {
            throw new PipelineNotFoundException(exactName, workspaceRef(workspaceId));
        }

        if (list.getPipelines().size() > 1) {
            throw new MultiplePipelinesFoundException(exactName, workspaceRef(workspaceId));
        }

        return list.getPipelines().get(0);
    }

    protected PipelineDbDto pipelineByName(Long workspaceId, String pipelineName) throws ApiException {
        return pipelineByName(workspaceId, pipelineName, NO_PIPELINE_ATTRIBUTES);
    }

    protected PipelineDbDto fetchPipeline(PipelineRefOptions pipelineRefOptions, Long wspId, PipelineQueryAttribute... attributes) throws ApiException {
        Long pipelineId = pipelineRefOptions.pipeline.pipelineId;
        if (pipelineId == null) {
            pipelineId = pipelineByName(wspId, pipelineRefOptions.pipeline.pipelineName).getPipelineId();
        }
        return pipelinesApi().describePipeline(pipelineId, List.of(attributes), wspId, null).getPipeline();
    }

    protected void throwPipelineNotFoundException(PipelineRefOptions pipelineRefOptions, Long wspId) throws ApiException, PipelineNotFoundException {
        if (pipelineRefOptions.pipeline.pipelineId != null) {
            throw new PipelineNotFoundException(pipelineRefOptions.pipeline.pipelineId, workspaceRef(wspId));
        }
        throw new PipelineNotFoundException(pipelineRefOptions.pipeline.pipelineName, workspaceRef(wspId));
    }

    protected PipelineVersionFullInfoDto findVersionByRef(Long pipelineId, Long wspId, VersionRefOptions.VersionRef ref) throws ApiException {
        String search = ref.versionName;
        Boolean isPublished = ref.versionName != null ? true : null;
        Predicate<PipelineVersionFullInfoDto> matcher = ref.versionId != null
                ? v -> ref.versionId.equals(v.getId())
                : v -> ref.versionName.equals(v.getName());

        ListPipelineVersionsResponse response = pipelineVersionsApi()
                .listPipelineVersions(pipelineId, wspId, null, null, search, isPublished);

        if (response.getVersions() == null) {
            throw new TowerException("No versions available for the pipeline, check if Pipeline versioning feature is enabled for the workspace");
        }

        return response.getVersions().stream()
                .map(PipelineDbDto::getVersion)
                .filter(Objects::nonNull)
                .filter(matcher)
                .findFirst()
                .orElse(null);
    }

    private static String quotePipelineName(String pipelineName) {

        if (pipelineName == null) return null;
        if (pipelineName.isEmpty()) return "\"\"";

        // replace '"' with '\"'
        String escaped = pipelineName.replace("\"", "\\\"");

        if (escaped.startsWith("\\\"") && escaped.endsWith("\\\"")) {
            return escaped;
        }

        return "\"" + escaped + "\"";
    }

}


