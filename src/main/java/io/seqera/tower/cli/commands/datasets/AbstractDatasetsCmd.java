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

package io.seqera.tower.cli.commands.datasets;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.DatasetNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.Dataset;
import io.seqera.tower.model.DatasetVersionDbDto;
import io.seqera.tower.model.ListDatasetVersionsResponse;
import io.seqera.tower.model.ListDatasetsResponse;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command
public abstract class AbstractDatasetsCmd extends AbstractApiCmd {

    protected Dataset datasetByName(Long workspaceId, String datasetName) throws ApiException {
        ListDatasetsResponse listDatasetsResponse = api().listDatasets(workspaceId);

        if (listDatasetsResponse == null || listDatasetsResponse.getDatasets() == null) {
            throw new DatasetNotFoundException(workspaceRef(workspaceId));
        }

        List<Dataset> datasetList = listDatasetsResponse.getDatasets().stream()
                .filter(it -> Objects.equals(it.getName(), datasetName))
                .collect(Collectors.toList());

        if (datasetList.isEmpty()) {
            throw new DatasetNotFoundException(datasetName, workspaceRef(workspaceId));
        }

        return datasetList.stream().findFirst().orElse(null);
    }

    protected List<Dataset> searchByName(Long workspaceId, String datasetName) throws ApiException {
        ListDatasetsResponse listDatasetsResponse = api().listDatasets(workspaceId);

        if (datasetName == null) {
            return listDatasetsResponse.getDatasets();
        }

        if (listDatasetsResponse == null || listDatasetsResponse.getDatasets() == null) {
            throw new DatasetNotFoundException(workspaceRef(workspaceId));
        }

        List<Dataset> datasetList = listDatasetsResponse.getDatasets().stream()
                .filter(it -> it.getName().startsWith(datasetName))
                .collect(Collectors.toList());

        if (datasetList.isEmpty()) {
            throw new DatasetNotFoundException(datasetName, workspaceRef(workspaceId));
        }

        return datasetList;
    }

    protected Dataset fetchDescribeDatasetResponse(DatasetRefOptions datasetRefOptions, Long wspId) throws ApiException {
        Dataset response;

        if (datasetRefOptions.dataset.datasetId != null) {
            response = api().describeDataset(wspId, datasetRefOptions.dataset.datasetId).getDataset();
        } else {
            response = datasetByName(wspId, datasetRefOptions.dataset.datasetName);
        }

        return response;
    }

    protected DatasetVersionDbDto fetchDatasetVersion(Long wspId, String datasetId, String datasetMediaType, Long version) throws ApiException {
        DatasetVersionDbDto datasetVersion;

        ListDatasetVersionsResponse listDatasetVersionsResponse = api().listDatasetVersions(wspId, datasetId, datasetMediaType);

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

    protected DatasetVersionDbDto getLatestVersion(ListDatasetVersionsResponse listDatasetVersionsResponse) {
        Comparator<DatasetVersionDbDto> versionComparator = Comparator.comparing(DatasetVersionDbDto::getVersion);

        return listDatasetVersionsResponse.getVersions()
                .stream()
                .max(versionComparator)
                .orElse(null);
    }

    protected DatasetVersionDbDto getFromVersion(ListDatasetVersionsResponse listDatasetVersionsResponse, Long version) {
        return listDatasetVersionsResponse.getVersions()
                .stream()
                .filter(it -> Objects.equals(it.getVersion(), version))
                .findFirst()
                .orElse(null);
    }

    protected String getDatasetRef(DatasetRefOptions datasetRefOptions) {
        return datasetRefOptions.dataset.datasetName != null ? datasetRefOptions.dataset.datasetName : datasetRefOptions.dataset.datasetId;
    }

    protected void deleteDatasetByName(String datasetName, Long wspId) throws DatasetNotFoundException, ApiException {
        Dataset response = datasetByName(wspId, datasetName);
        deleteDatasetById(response.getId(), wspId);
    }

    protected void deleteDatasetById(String datasetId, Long wspId) throws DatasetNotFoundException, ApiException {
        api().deleteDataset(wspId, datasetId);
    }


}
