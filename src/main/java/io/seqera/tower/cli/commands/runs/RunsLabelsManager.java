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

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.labels.BaseLabelsManager;
import io.seqera.tower.model.AssociateWorkflowLabelsRequest;

import java.util.List;

public class RunsLabelsManager extends BaseLabelsManager<AssociateWorkflowLabelsRequest, String> {

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
