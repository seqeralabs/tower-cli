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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.DatasetNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.DatasetDto;
import io.seqera.tower.model.DatasetVersionDto;
import io.seqera.tower.model.ListDatasetVersionsResponse;
import io.seqera.tower.model.ListDatasetsResponse;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command
public abstract class AbstractDatasetsCmd extends AbstractApiCmd {

    protected DatasetDto datasetByName(Long workspaceId, String datasetName) throws ApiException {
        ListDatasetsResponse listDatasetsResponse = datasetsApi().listDatasetsV2(workspaceId, null, null, datasetName, null, null, "all", List.of());

        if (listDatasetsResponse == null || listDatasetsResponse.getDatasets() == null) {
            throw new DatasetNotFoundException(workspaceRef(workspaceId));
        }

        List<DatasetDto> datasetList = listDatasetsResponse.getDatasets().stream()
                .filter(it -> Objects.equals(it.getName(), datasetName))
                .collect(Collectors.toList());

        if (datasetList.isEmpty()) {
            throw new DatasetNotFoundException(datasetName, workspaceRef(workspaceId));
        }

        return datasetList.get(0);
    }

    protected DatasetDto fetchDescribeDatasetResponse(DatasetRefOptions datasetRefOptions, Long wspId) throws ApiException {
        DatasetDto response;

        if (datasetRefOptions.dataset.datasetId != null) {
            response = datasetsApi().describeDatasetV2(datasetRefOptions.dataset.datasetId, wspId, List.of()).getDataset();
        } else {
            response = datasetByName(wspId, datasetRefOptions.dataset.datasetName);
        }

        return response;
    }

    protected DatasetVersionDto fetchDatasetVersion(Long wspId, String datasetId, String datasetMediaType, Long version) throws ApiException {
        DatasetVersionDto datasetVersion;

        ListDatasetVersionsResponse listDatasetVersionsResponse = datasetsApi().listDatasetVersionsV2(datasetId, wspId, datasetMediaType);

        if (listDatasetVersionsResponse == null || listDatasetVersionsResponse.getVersions() == null) {
            throw new TowerException(String.format("No versions were found for dataset %s", datasetId));
        }

        if (version == null) {
            datasetVersion = getLatestVersion(listDatasetVersionsResponse);
        } else {
            datasetVersion = getFromVersion(listDatasetVersionsResponse, version);
        }

        if (datasetVersion == null) {
            throw new TowerException(String.format("No versions were found that matches version %s for dataset %s", version, datasetId));
        }

        return datasetVersion;
    }

    protected DatasetVersionDto getLatestVersion(ListDatasetVersionsResponse listDatasetVersionsResponse) {
        Comparator<DatasetVersionDto> versionComparator = Comparator.comparing(DatasetVersionDto::getVersion);

        return listDatasetVersionsResponse.getVersions()
                .stream()
                .max(versionComparator)
                .orElse(null);
    }

    protected DatasetVersionDto getFromVersion(ListDatasetVersionsResponse listDatasetVersionsResponse, Long version) {
        return listDatasetVersionsResponse.getVersions()
                .stream()
                .filter(it -> Objects.equals(it.getVersion(), version))
                .findFirst()
                .orElse(null);
    }

    protected String getDatasetRef(DatasetRefOptions datasetRefOptions) {
        return datasetRefOptions.dataset.datasetName != null ? datasetRefOptions.dataset.datasetName : datasetRefOptions.dataset.datasetId;
    }

    protected List<String> resolveDatasetIds(DatasetMultiRefOptions options, Long wspId) throws ApiException {
        List<String> ids = new ArrayList<>();
        if (options.dataset.datasetIds != null) {
            ids.addAll(options.dataset.datasetIds);
        }
        if (options.dataset.datasetNames != null) {
            for (String name : options.dataset.datasetNames) {
                ids.add(datasetByName(wspId, name).getId());
            }
        }
        return ids;
    }

    protected void deleteDatasetByName(String datasetName, Long wspId) throws DatasetNotFoundException, ApiException {
        DatasetDto response = datasetByName(wspId, datasetName);
        deleteDatasetById(response.getId(), wspId);
    }

    protected void deleteDatasetById(String datasetId, Long wspId) throws DatasetNotFoundException, ApiException {
        datasetsApi().deleteDatasetV2(datasetId, wspId);
    }


}
