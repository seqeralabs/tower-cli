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

package io.seqera.tower.cli.commands.data.links;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.datastudios.DataLinkRefOptions;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.DataLinkNotFoundException;
import io.seqera.tower.cli.exceptions.MultipleDataLinksFoundException;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.utils.ResponseHelper;
import io.seqera.tower.model.DataLinkDto;

public class DataLinkService  {

    protected final DefaultApi api;
    protected final Tower app;

    public DataLinkService(DefaultApi api, Tower app) {
        this.api = api;
        this.app = app;
    }

    public boolean checkIfResultIncomplete(Long wspId, String credId, boolean wait) {
        DataLinksFetchStatus status = checkDataLinksFetchStatus(wspId, credId);
        if (wait && status == DataLinksFetchStatus.FETCHING) {
            waitForDoneStatus(wspId, credId);
        }

        return !wait && status == DataLinksFetchStatus.FETCHING;
    }

    void waitForDoneStatus(Long wspId, String credId) {
        try {
            ResponseHelper.waitStatus(
                    app.getOut(),
                    app.output != OutputType.json,
                    DataLinksFetchStatus.DONE,
                    DataLinksFetchStatus.values(),
                    () -> checkDataLinksFetchStatus(wspId, credId),
                    DataLinksFetchStatus.DONE, DataLinksFetchStatus.ERROR
            );
        } catch (InterruptedException e) {
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

    public List<String> getDataLinkIds(DataLinkRefOptions.DataLinkRef dataLinkRef, Long wspId) {
        // if DataLink IDs are supplied - use those directly
        if (dataLinkRef.mountDataIds != null) {
            return dataLinkRef.mountDataIds;
        }

        // Check and wait if DataLinks are still being fetched
        boolean isResultIncomplete = checkIfResultIncomplete(wspId, null, true);
        if (isResultIncomplete) {
            throw new TowerRuntimeException("Failed to fetch datalinks for mountData - please retry.");
        }

        List<String> dataLinkIds = new ArrayList<>();

        if (dataLinkRef.mountDataNames != null) {
            dataLinkIds = dataLinkRef.mountDataNames.stream()
                    .map(name -> getDataLinkIdByName(wspId, name))
                    .collect(Collectors.toList());
        }

        if (dataLinkRef.mountDataResourceRefs != null) {
            dataLinkIds = dataLinkRef.mountDataResourceRefs.stream()
                    .map(resourceRef -> getDataLinkIdByResourceRef(wspId, resourceRef))
                    .collect(Collectors.toList());
        }

        return dataLinkIds;
    }

    private String getDataLinkIdByName(Long wspId, String name) {
        return getDataLinkIdsBySearchAndFindExactMatch(wspId, name, datalink -> name.equals(datalink.getName()));
    }

    private String getDataLinkIdByResourceRef(Long wspId, String resourceRef) {
        return getDataLinkIdsBySearchAndFindExactMatch(wspId, getResourceRefKeywordParam(resourceRef), datalink -> resourceRef.equals(datalink.getResourceRef()));
    }

    private String getDataLinkIdsBySearchAndFindExactMatch(Long wspId, String search, Predicate<DataLinkDto> filter) {
        var datalinks = getDataLinksBySearchCriteria(wspId, search).stream()
                .filter(filter)
                .collect(Collectors.toList());

        if (datalinks.isEmpty()) {
            throw new DataLinkNotFoundException(search, wspId);
        }

        if (datalinks.size() > 1) {
            var dataLinkIds = datalinks.stream().map(DataLinkDto::getId).collect(Collectors.toList());
            throw new MultipleDataLinksFoundException(search, wspId, dataLinkIds);
        }

        return datalinks.get(0).getId();
    }

    private List<DataLinkDto> getDataLinksBySearchCriteria(Long wspId, String search) {
        try {
            return api.listDataLinks(wspId, null, search, null, null, null).getDataLinks();
        } catch (ApiException e) {
            throw new TowerRuntimeException("Encountered error while retrieving data links for " + search, e);
        }
    }

    private String getResourceRefKeywordParam(String resourceRef) {
        return String.format("resourceRef:%s", resourceRef);
    }

}
