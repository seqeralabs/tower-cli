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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.LabelsApi;
import io.seqera.tower.cli.responses.labels.ManageLabels;

import java.util.List;

public abstract class BaseLabelsManager<Request, EntityId> {

    protected final LabelsApi api;
    private final LabelsFinder finder;
    private final String type;

    public BaseLabelsManager(LabelsApi api, String type) {
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
        final List<Long> labelsIds = finder.findLabelsIds(wspId, labels, selectBehavior(noCreate,operation));
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

    private LabelsFinder.NotFoundLabelBehavior selectBehavior(boolean noCreate, LabelsSubcmdOptions.Operation operation) {
        if (operation == LabelsSubcmdOptions.Operation.delete) {
            // for delete operations we never want to create non-existing labels
            // we can simply skip the label
            return LabelsFinder.NotFoundLabelBehavior.FILTER;
        }
        return noCreate? LabelsFinder.NotFoundLabelBehavior.FAIL: LabelsFinder.NotFoundLabelBehavior.CREATE;
    }
}
