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

package io.seqera.tower.cli.commands.pipelines;

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.LabelsApi;
import io.seqera.tower.cli.commands.labels.BaseLabelsManager;
import io.seqera.tower.model.AssociatePipelineLabelsRequest;

public class PipelinesLabelsManager extends BaseLabelsManager<AssociatePipelineLabelsRequest, Long> {
    public PipelinesLabelsManager(LabelsApi api) {
        super(api, "pipeline");
    }

    @Override
    protected AssociatePipelineLabelsRequest getRequest(List<Long> labelsIds, Long entityId) {
        return new AssociatePipelineLabelsRequest().labelIds(labelsIds).pipelineIds(List.of(entityId));
    }

    @Override
    protected void apply(AssociatePipelineLabelsRequest associatePipelineLabelsRequest, Long wspId) throws ApiException {
        api.applyLabelsToPipelines(associatePipelineLabelsRequest, wspId);
    }

    @Override
    protected void remove(AssociatePipelineLabelsRequest associatePipelineLabelsRequest, Long wspId) throws ApiException {
        api.removeLabelsFromPipelines(associatePipelineLabelsRequest, wspId);
    }

    @Override
    protected void append(AssociatePipelineLabelsRequest associatePipelineLabelsRequest, Long wspId) throws ApiException {
        api.addLabelsToPipelines(associatePipelineLabelsRequest, wspId);
    }
}
