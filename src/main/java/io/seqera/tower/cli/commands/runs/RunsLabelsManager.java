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

package io.seqera.tower.cli.commands.runs;

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.labels.BaseLabelsManager;
import io.seqera.tower.model.AssociateWorkflowLabelsRequest;

public class RunsLabelsManager extends BaseLabelsManager<AssociateWorkflowLabelsRequest,String> {

    public RunsLabelsManager(DefaultApi api) {
        super(api, "run");
    }

    @Override
    protected AssociateWorkflowLabelsRequest getRequest(List<Long> labelsIds, String entityId) {
        return new AssociateWorkflowLabelsRequest().labelIds(labelsIds).workflowIds(List.of(entityId));
    }

    @Override
    protected void apply(AssociateWorkflowLabelsRequest associateWorkflowLabelsRequest, Long wspId) throws ApiException {
        api.applyLabelsToWorkflows(associateWorkflowLabelsRequest, wspId);
    }

    @Override
    protected void remove(AssociateWorkflowLabelsRequest associateWorkflowLabelsRequest, Long wspId) throws ApiException {
        api.removeLabelsFromWorkflows(associateWorkflowLabelsRequest, wspId);
    }

    @Override
    protected void append(AssociateWorkflowLabelsRequest associateWorkflowLabelsRequest, Long wspId) throws ApiException {
        api.addLabelsToWorkflows(associateWorkflowLabelsRequest, wspId);
    }
}
