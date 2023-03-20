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
