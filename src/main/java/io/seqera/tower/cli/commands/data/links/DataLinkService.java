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

package io.seqera.tower.cli.commands.data.links;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DataLinksApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.DataLinkNotFoundException;
import io.seqera.tower.cli.exceptions.MultipleDataLinksFoundException;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.utils.ResponseHelper;
import io.seqera.tower.model.DataLinkDto;

public class DataLinkService  {

    protected final DataLinksApi api;
    protected final Tower app;

    public DataLinkService(DataLinksApi api, Tower app) {
        this.api = api;
        this.app = app;
    }

    public boolean checkIfResultIncomplete(Long wspId, String credId, boolean wait) {
        DataLinksFetchStatus status = checkDataLinksFetchStatus(wspId, credId);
        if (wait && status == DataLinksFetchStatus.FETCHING) {
            boolean showProgress = app.output != OutputType.json;
            if (showProgress) {
                app.getOut().println(" Fetching data-links.");
            }
            waitForDoneStatus(wspId, credId, showProgress);
        }

        return !wait && status == DataLinksFetchStatus.FETCHING;
    }

    void waitForDoneStatus(Long wspId, String credId, boolean showProgress) {
        try {
            ResponseHelper.waitStatus(
                    app.getOut(),
                    showProgress,
                    DataLinksFetchStatus.DONE,
                    DataLinksFetchStatus.values(),
                    () -> checkDataLinksFetchStatus(wspId, credId),
                    DataLinksFetchStatus.DONE, DataLinksFetchStatus.ERROR
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    DataLinksFetchStatus checkDataLinksFetchStatus(Long wspId, String credentialsId) {
        int status;
        try {
            status = api.listDataLinksWithHttpInfo(wspId, credentialsId, null, 1, 0, null).getStatusCode();
        } catch (ApiException e) {
            return DataLinksFetchStatus.ERROR;
        }
        switch (status) {
            case 200:
                return DataLinksFetchStatus.DONE;
            case 202:
                return DataLinksFetchStatus.FETCHING;
            default:
                return DataLinksFetchStatus.ERROR;
        }
    }

    public enum DataLinksFetchStatus {
        FETCHING, DONE, ERROR
    }

    public DataLinkDto getDataLink(DataLinkRefOptions.DataLinkRef dataLinkRef, Long wspId, String credId) {
        // if DataLink IDs are supplied - use those directly
        if (dataLinkRef.dataLinkId != null) {
            return getDataLinkById(dataLinkRef.dataLinkId, wspId, credId);
        }

        // Check and wait if DataLinks are still being fetched
        boolean isResultIncomplete = checkIfResultIncomplete(wspId, credId, true);
        if (isResultIncomplete) {
            throw new TowerRuntimeException("Failed to fetch datalinks for datalink - please retry.");
        }

        if (dataLinkRef.dataLinkName != null) {
            return getDataLinkByName(wspId, credId, dataLinkRef.dataLinkName);
        }

        if (dataLinkRef.dataLinkUri != null) {
            return getDataLinkByResourceRef(wspId, credId, dataLinkRef.dataLinkUri);
        }

        return null;
    }

    public List<String> getDataLinkIds(io.seqera.tower.cli.commands.studios.DataLinkRefOptions.DataLinkRef dataLinkRef, Long wspId) {
        // if DataLink IDs are supplied - use those directly
        if (dataLinkRef.getMountDataIds() != null) {
            return dataLinkRef.getMountDataIds();
        }

        // Check and wait if DataLinks are still being fetched
        boolean isResultIncomplete = checkIfResultIncomplete(wspId, null, true);
        if (isResultIncomplete) {
            throw new TowerRuntimeException("Failed to fetch datalinks for mountData - please retry.");
        }

        List<String> dataLinkIds = new ArrayList<>();

        if (dataLinkRef.getMountDataNames() != null) {
            dataLinkIds = dataLinkRef.getMountDataNames().stream()
                    .map(name -> getDataLinkByName(wspId, null, name).getId())
                    .collect(Collectors.toList());
        }

        if (dataLinkRef.getMountDataUris() != null) {
            dataLinkIds = dataLinkRef.getMountDataUris().stream()
                    .map(resourceRef -> getDataLinkByResourceRef(wspId, null, resourceRef).getId())
                    .collect(Collectors.toList());
        }

        return dataLinkIds;
    }

    private DataLinkDto getDataLinkByName(Long wspId, String credId, String name) {
        return getDataLinkBySearchAndFindExactMatch(wspId, name, credId, datalink -> name.equals(datalink.getName()));
    }

    private DataLinkDto getDataLinkByResourceRef(Long wspId, String credId, String resourceRef) {
        return getDataLinkBySearchAndFindExactMatch(wspId, getResourceRefKeywordParam(resourceRef), credId, datalink -> resourceRef.equals(datalink.getResourceRef()));
    }

    private DataLinkDto getDataLinkBySearchAndFindExactMatch(Long wspId, String search, String credId, Predicate<DataLinkDto> filter) {
        var datalinks = getDataLinksBySearchCriteria(wspId, search, credId).stream()
                .filter(filter)
                .collect(Collectors.toList());

        if (datalinks.isEmpty()) {
            throw new DataLinkNotFoundException(search, wspId);
        }

        if (datalinks.size() > 1) {
            var dataLinkIds = datalinks.stream().map(DataLinkDto::getId).collect(Collectors.toList());
            throw new MultipleDataLinksFoundException(search, wspId, dataLinkIds);
        }

        return datalinks.get(0);
    }

    private DataLinkDto getDataLinkById(String dataLinkId, Long wspId, String credId) {
        try {
            return api.describeDataLink(dataLinkId, wspId, credId).getDataLink();
        } catch (ApiException e) {
            throw new TowerRuntimeException("Encountered error while retrieving data link for id " + dataLinkId, e);
        }
    }

    private List<DataLinkDto> getDataLinksBySearchCriteria(Long wspId, String search, String credId) {
        try {
            return api.listDataLinks(wspId, credId, search, null, null, null).getDataLinks();
        } catch (ApiException e) {
            throw new TowerRuntimeException("Encountered error while retrieving data links for " + search, e);
        }
    }

    private String getResourceRefKeywordParam(String resourceRef) {
        return String.format("resourceRef:%s", resourceRef);
    }

}
