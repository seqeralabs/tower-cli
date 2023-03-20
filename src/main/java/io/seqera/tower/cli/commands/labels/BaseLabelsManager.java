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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.responses.labels.ManageLabels;

import java.util.List;

public abstract class BaseLabelsManager<Request, EntityId> {

    protected final DefaultApi api;
    private final LabelsFinder finder;
    private final String type;

    public BaseLabelsManager(DefaultApi api, String type) {
        this.api = api;
        this.finder = new LabelsFinder(api);
        this.type = type;
    }

    protected abstract Request getRequest(List<Long> labelsIds, EntityId entityId);


    protected abstract void apply(Request request, Long wspId) throws ApiException;

    protected abstract void remove(Request request, Long wspId) throws ApiException;

    protected abstract void append(Request request, Long wspId) throws ApiException;


    public void execute(Long wspId, EntityId entityId, List<Label> labels) throws ApiException {
        if (labels == null || labels.isEmpty()) {
            return;
        }
        execute(wspId, entityId, labels, LabelsSubcmdOptions.Operation.set, false);
    }

    public ManageLabels execute(Long wspId, EntityId entityId, LabelsSubcmdOptions options) throws ApiException {
        return execute(wspId, entityId, options.getLabels(), options.getOperation(), options.getNoCreate());
    }

    public ManageLabels execute(Long wspId, EntityId entityId, List<Label> labels, LabelsSubcmdOptions.Operation operation, boolean noCreate) throws ApiException {
        final List<Long> labelsIds = finder.findLabelsIds(wspId, labels, noCreate);
        final Request request = getRequest(labelsIds, entityId);
        switch (operation) {
            case set:
                apply(request, wspId);
                break;
            case append:
                append(request, wspId);
                break;
            case delete:
                remove(request, wspId);
                break;
        }

        return new ManageLabels(operation.prettyName, this.type, entityId.toString(), wspId);
    }
}
