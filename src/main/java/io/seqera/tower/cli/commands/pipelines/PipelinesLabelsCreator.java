/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.pipelines;

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.labels.LabelsCreator;
import io.seqera.tower.model.AssociatePipelineLabelsRequest;

public class PipelinesLabelsCreator extends LabelsCreator<AssociatePipelineLabelsRequest,Long> {
    public PipelinesLabelsCreator(DefaultApi api) {
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
        api.removeLabelsFromPipelines(associatePipelineLabelsRequest,wspId);
    }

    @Override
    protected void append(AssociatePipelineLabelsRequest associatePipelineLabelsRequest, Long wspId) throws ApiException {
        api.removeLabelsFromPipelines(associatePipelineLabelsRequest,wspId);
    }
}
