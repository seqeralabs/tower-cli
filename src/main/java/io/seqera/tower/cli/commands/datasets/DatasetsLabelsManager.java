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

package io.seqera.tower.cli.commands.datasets;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.LabelsApi;
import io.seqera.tower.cli.commands.labels.BaseLabelsManager;
import io.seqera.tower.model.AssociateDatasetsLabelsRequest;

import java.util.List;

public class DatasetsLabelsManager extends BaseLabelsManager<AssociateDatasetsLabelsRequest, String> {

    public DatasetsLabelsManager(LabelsApi api) {
        super(api, "dataset");
    }

    @Override
    protected AssociateDatasetsLabelsRequest getRequest(List<Long> labelsIds, String entityId) {
        return new AssociateDatasetsLabelsRequest().labelIds(labelsIds).datasetIds(List.of(entityId));
    }

    @Override
    protected void apply(AssociateDatasetsLabelsRequest request, Long wspId) throws ApiException {
        api.applyLabelsToDatasets(request, wspId);
    }

    @Override
    protected void remove(AssociateDatasetsLabelsRequest request, Long wspId) throws ApiException {
        api.removeLabelsFromDatasets(request, wspId);
    }

    @Override
    protected void append(AssociateDatasetsLabelsRequest request, Long wspId) throws ApiException {
        api.addLabelsToDatasets(request, wspId);
    }
}
