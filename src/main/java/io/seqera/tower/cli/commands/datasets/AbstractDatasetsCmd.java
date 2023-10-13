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
