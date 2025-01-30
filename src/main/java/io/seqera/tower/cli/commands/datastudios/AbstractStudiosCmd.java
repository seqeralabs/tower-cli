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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.data.links.DataLinkService;
import io.seqera.tower.cli.exceptions.DataStudioNotFoundException;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioProgressStep;
import io.seqera.tower.model.DataStudioStatus;
import io.seqera.tower.model.DataStudioStatusInfo;
import io.seqera.tower.model.DataStudioTemplate;
import io.seqera.tower.model.DataStudioTemplatesListResponse;

import static io.seqera.tower.cli.utils.ResponseHelper.waitStatus;
import static io.seqera.tower.model.DataStudioProgressStepStatus.ERRORED;
import static io.seqera.tower.model.DataStudioProgressStepStatus.IN_PROGRESS;

public class AbstractStudiosCmd extends AbstractApiCmd {

    private static final Integer DEFAULT_MAX_TEMPLATES_TO_QUERY = 20;
    protected String getSessionId(DataStudioRefOptions dataStudioRefOptions, Long wspId) throws ApiException {
        return dataStudioRefOptions.dataStudio.sessionId != null
                ? dataStudioRefOptions.dataStudio.sessionId
                : fetchDataStudio(dataStudioRefOptions, wspId).getSessionId();
    }

    protected DataStudioDto fetchDataStudio(DataStudioRefOptions dataStudioRefOptions, Long wspId) throws ApiException {
        DataStudioDto dataStudio;

        if (dataStudioRefOptions.dataStudio.sessionId != null) {
            dataStudio = getDataStudioById(wspId, dataStudioRefOptions.dataStudio.sessionId);
        } else {
            dataStudio = getDataStudioByName(wspId, dataStudioRefOptions.dataStudio.dataStudioName );
        }

        return dataStudio;
    }

    protected String getParentDataStudioSessionId(ParentDataStudioRefOptions parentDataStudioRefOptions, Long wspId) throws ApiException {
        if (parentDataStudioRefOptions.studio.sessionId != null) {
            return parentDataStudioRefOptions.studio.sessionId;
        } else {
            DataStudioDto dataStudio = getDataStudioByName(wspId, parentDataStudioRefOptions.studio.name);
            return dataStudio.getSessionId();
        }
    }

    protected List<DataStudioTemplate> fetchDataStudioTemplates(Long workspaceId) throws ApiException {
        return fetchDataStudioTemplates(workspaceId,DEFAULT_MAX_TEMPLATES_TO_QUERY);
    }

    protected List<DataStudioTemplate> fetchDataStudioTemplates(Long workspaceId, Integer max) throws ApiException {
        DataStudioTemplatesListResponse response = api().listDataStudioTemplates(workspaceId,max,0);
        return response.getTemplates();
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
            Thread.currentThread().interrupt();
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

    protected DataStudioConfiguration dataStudioConfigurationFrom(Long wspId, DataStudioConfigurationOptions configurationOptions, String condaEnvOverride) throws ApiException {
        return dataStudioConfigurationFrom(wspId, null, configurationOptions, condaEnvOverride);
    }
    protected DataStudioConfiguration dataStudioConfigurationFrom(Long wspId, DataStudioDto baseStudio, DataStudioConfigurationOptions configurationOptions) throws ApiException {
        return dataStudioConfigurationFrom(wspId, baseStudio, configurationOptions, null);
    }
    protected DataStudioConfiguration dataStudioConfigurationFrom(Long wspId, DataStudioDto baseStudio, DataStudioConfigurationOptions configOptions, String condaEnvOverride) throws ApiException {
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

        dataStudioConfiguration.setMountData(getMountDataIds(configOptions, dataStudioConfiguration, wspId));


        if (condaEnvOverride != null && !condaEnvOverride.isEmpty()) {
            dataStudioConfiguration.setCondaEnvironment(condaEnvOverride);
        }

        return dataStudioConfiguration;
    }

    List<String> getMountDataIds(DataStudioConfigurationOptions dataStudioConfigOptions, DataStudioConfiguration currentDataStudioConfiguration, Long wspId) throws ApiException {
        if (dataStudioConfigOptions.dataLinkRefOptions == null || dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef == null) {
            return currentDataStudioConfiguration.getMountData();
        }

        DataLinkService dataLinkService = new DataLinkService(api(), app());
        return dataLinkService.getDataLinkIds(dataStudioConfigOptions.dataLinkRefOptions.dataLinkRef, wspId);
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
