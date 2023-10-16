/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.commands.actions;

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.labels.BaseLabelsManager;
import io.seqera.tower.model.AssociateActionLabelsRequest;

public class ActionsLabelsManager extends BaseLabelsManager<AssociateActionLabelsRequest, String> {


    public ActionsLabelsManager(DefaultApi api) {
        super(api, "action");
    }

    @Override
    protected AssociateActionLabelsRequest getRequest(List<Long> labelsIds, String entityId) {
        return new AssociateActionLabelsRequest().labelIds(labelsIds).actionIds(List.of(entityId));
    }

    @Override
    protected void apply(AssociateActionLabelsRequest associateActionLabelsRequest, Long wspId) throws ApiException {
        api.applyLabelsToActions(associateActionLabelsRequest, wspId);
    }

    @Override
    protected void remove(AssociateActionLabelsRequest associateActionLabelsRequest, Long wspId) throws ApiException {
        api.removeLabelsFromActions(associateActionLabelsRequest, wspId);
    }

    @Override
    protected void append(AssociateActionLabelsRequest associateActionLabelsRequest, Long wspId) throws ApiException {
        api.addLabelsToActions(associateActionLabelsRequest, wspId);
    }
}
