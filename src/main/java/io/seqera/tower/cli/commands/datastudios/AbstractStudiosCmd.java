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

package io.seqera.tower.cli.commands.datastudios;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.data.links.AbstractDataLinkCmd;
import io.seqera.tower.cli.exceptions.DataLinkNotFoundException;
import io.seqera.tower.cli.exceptions.DataStudioNotFoundException;
import io.seqera.tower.cli.exceptions.MultipleDataLinksFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.model.DataLinkDto;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioProgressStep;
import io.seqera.tower.model.DataStudioStatus;
import io.seqera.tower.model.DataStudioStatusInfo;

import static io.seqera.tower.cli.utils.ResponseHelper.waitStatus;
import static io.seqera.tower.model.DataStudioProgressStepStatus.ERRORED;
import static io.seqera.tower.model.DataStudioProgressStepStatus.IN_PROGRESS;

public class AbstractStudiosCmd extends AbstractDataLinkCmd {

    protected DataStudioDto fetchDataStudio(DataStudioRefOptions dataStudioRefOptions, Long wspId) throws ApiException {
        DataStudioDto dataStudio;

        if (dataStudioRefOptions.dataStudio.sessionId != null) {
            dataStudio = getDataStudioById(wspId, dataStudioRefOptions.dataStudio.sessionId);
        } else {
            dataStudio = getDataStudioByName(wspId, dataStudioRefOptions.dataStudio.dataStudioName );
        }

        return dataStudio;
    }

    private DataStudioDto getDataStudioByName(Long wspId, String dataStudioName) throws ApiException {
        List<DataStudioDto> studios = api().listDataStudios(wspId, null, null, null).getStudios();

        return studios.stream()
                .filter(s -> dataStudioName.equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new DataStudioNotFoundException(dataStudioName, wspId));
    }

    private DataStudioDto getDataStudioById(Long wspId, String sessionId) throws ApiException {
        return api().describeDataStudio(sessionId, wspId);
    }

    protected Integer onBeforeExit(int exitCode, String sessionId, Long workspaceId, DataStudioStatus targetStatus) {
        boolean showProgress = app().output != OutputType.json;

        try {
            return waitStatus(
                    app().getOut(),
                    showProgress,
                    new ProgressStepMessageSupplier(sessionId, workspaceId),
                    targetStatus,
                    DataStudioStatus.values(),
                    () -> checkDataStudioStatus(sessionId, workspaceId),
                    DataStudioStatus.STOPPED, DataStudioStatus.ERRORED, DataStudioStatus.RUNNING
            );
        } catch (InterruptedException e) {
            return exitCode;
        }
    }

    protected DataStudioStatus checkDataStudioStatus(String sessionId, Long workspaceId) {
        try {
            DataStudioStatusInfo statusInfo = api().describeDataStudio(sessionId, workspaceId).getStatusInfo();
            return statusInfo == null ? null : statusInfo.getStatus();
        } catch (ApiException e) {
            return null;
        }
    }

    protected DataStudioConfiguration dataStudioConfigurationFrom(DataStudioConfigurationOptions configurationOptions, String condaEnvOverride) {
        return dataStudioConfigurationFrom(null, configurationOptions, condaEnvOverride);
    }
    protected DataStudioConfiguration dataStudioConfigurationFrom(DataStudioDto baseStudio, DataStudioConfigurationOptions configurationOptions){
        return dataStudioConfigurationFrom(baseStudio, configurationOptions, null);
    }
    protected DataStudioConfiguration dataStudioConfigurationFrom(DataStudioDto baseStudio, DataStudioConfigurationOptions configOptions, String condaEnvOverride) {
        DataStudioConfiguration dataStudioConfiguration = baseStudio == null || baseStudio.getConfiguration() == null
                ? new DataStudioConfiguration()
                : baseStudio.getConfiguration();

        dataStudioConfiguration.setGpu(configOptions.gpu == null
                ? dataStudioConfiguration.getGpu()
                : configOptions.gpu);
        dataStudioConfiguration.setCpu(configOptions.cpu == null
                ? dataStudioConfiguration.getCpu()
                : configOptions.cpu);
        dataStudioConfiguration.setMemory(configOptions.memory == null
                ? dataStudioConfiguration.getMemory()
                : configOptions.memory);
        dataStudioConfiguration.setMountData(configOptions.mountData == null || configOptions.mountData.isEmpty()
                ? dataStudioConfiguration.getMountData()
                : configOptions.mountData);

        if (condaEnvOverride != null && !condaEnvOverride.isEmpty()) {
            dataStudioConfiguration.setCondaEnvironment(condaEnvOverride);
        }

        return dataStudioConfiguration;
    }

    List<String> getMountDataIds(DataStudioConfigurationOptions dataStudioConfigOptions, DataStudioConfiguration currentDataStudioConfiguration, Long wspId) throws ApiException {
        if (dataStudioConfigOptions.dataLinkRefOptions == null || dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef == null) {
            return currentDataStudioConfiguration.getMountData();
        }

        // if DataLink IDs are supplied - use those in request directly
        if (dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef.mountDataIds != null) {
            return dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef.mountDataIds;
        }

        // Check and wait if DataLinks are still being fetched
        boolean isResultIncomplete = checkIfResultIncomplete(wspId, null, true);
        if (isResultIncomplete) {
            throw new TowerException("Failed to fetch datalinks for mountData - please retry.");
        }

        List<String> dataLinkIds = new ArrayList<>();

        if (dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef.mountDataNames != null) {
            dataLinkIds = dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef.mountDataNames.stream()
                    .map(name -> getDataLinkIdByName(wspId, name))
                    .collect(Collectors.toList());
        }

        if (dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef.mountDataResourceRefs != null) {
            dataLinkIds = dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef.mountDataResourceRefs.stream()
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
            return api().listDataLinks(wspId, null, search, null, null, null).getDataLinks();
        } catch (ApiException e) {
            throw new TowerRuntimeException("Encountered error while retrieving data links for " + search, e);
        }
    }

    private String getResourceRefKeywordParam(String resourceRef) {
        return String.format("resourceRef:%s", resourceRef);
    }

    public class ProgressStepMessageSupplier implements Supplier<String> {

        private final String sessionId;
        private final Long workspaceId;
        private DataStudioProgressStep currentProgressStep;

        public ProgressStepMessageSupplier(String sessionId, Long workspaceId) {
            this.sessionId = sessionId;
            this.workspaceId = workspaceId;
            this.currentProgressStep = new DataStudioProgressStep();
        }

        @Override
        public String get() {
            try {
                DataStudioDto dataStudioDto = api().describeDataStudio(sessionId, workspaceId);

                Optional<DataStudioProgressStep> inProgressStep = dataStudioDto.getProgress().stream()
                        .filter(step -> step.getStatus() == IN_PROGRESS || step.getStatus() == ERRORED)
                        .findFirst();

                if (inProgressStep.isPresent() && !inProgressStep.get().equals(currentProgressStep)) {
                    currentProgressStep = inProgressStep.get();
                    return currentProgressStep.getStatus() != ERRORED
                            ? String.format("\n  %s", currentProgressStep.getMessage())
                            : String.format("\n  %s - Error encountered: %s", currentProgressStep.getMessage(), dataStudioDto.getStatusInfo().getMessage());
                }

                return "";
            } catch (Exception e) {
                return "";
            }
        }
    }
}
