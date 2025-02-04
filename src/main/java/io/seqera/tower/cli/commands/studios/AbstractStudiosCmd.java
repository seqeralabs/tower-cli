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

package io.seqera.tower.cli.commands.studios;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.data.links.DataLinkService;
import io.seqera.tower.cli.exceptions.StudioNotFoundException;
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
    protected String getSessionId(StudioRefOptions studioRefOptions, Long wspId) throws ApiException {
        return studioRefOptions.studio.sessionId != null
                ? studioRefOptions.studio.sessionId
                : fetchStudio(studioRefOptions, wspId).getSessionId();
    }

    protected DataStudioDto fetchStudio(StudioRefOptions studioRefOptions, Long wspId) throws ApiException {
        DataStudioDto studio;

        if (studioRefOptions.studio.sessionId != null) {
            studio = getStudioById(wspId, studioRefOptions.studio.sessionId);
        } else {
            studio = getStudioByName(wspId, studioRefOptions.studio.studioName);
        }

        return studio;
    }

    protected String getParentStudioSessionId(ParentStudioRefOptions parentStudioRefOptions, Long wspId) throws ApiException {
        if (parentStudioRefOptions.studio.sessionId != null) {
            return parentStudioRefOptions.studio.sessionId;
        } else {
            DataStudioDto studio = getStudioByName(wspId, parentStudioRefOptions.studio.name);
            return studio.getSessionId();
        }
    }

    protected List<DataStudioTemplate> fetchStudioTemplates(Long workspaceId) throws ApiException {
        return fetchStudioTemplates(workspaceId,DEFAULT_MAX_TEMPLATES_TO_QUERY);
    }

    protected List<DataStudioTemplate> fetchStudioTemplates(Long workspaceId, Integer max) throws ApiException {
        DataStudioTemplatesListResponse response = api().listDataStudioTemplates(workspaceId,max,0);
        return response.getTemplates();
    }

    private DataStudioDto getStudioByName(Long wspId, String studioName) throws ApiException {
        List<DataStudioDto> studios = api().listDataStudios(wspId, null, null, null).getStudios();

        return studios.stream()
                .filter(s -> studioName.equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new StudioNotFoundException(studioName, wspId));
    }

    private DataStudioDto getStudioById(Long wspId, String sessionId) throws ApiException {
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
                    () -> checkStudioStatus(sessionId, workspaceId),
                    DataStudioStatus.STOPPED, DataStudioStatus.ERRORED, DataStudioStatus.RUNNING
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return exitCode;
        }
    }

    protected DataStudioStatus checkStudioStatus(String sessionId, Long workspaceId) {
        try {
            DataStudioStatusInfo statusInfo = api().describeDataStudio(sessionId, workspaceId).getStatusInfo();
            return statusInfo == null ? null : statusInfo.getStatus();
        } catch (ApiException e) {
            return null;
        }
    }

    protected DataStudioConfiguration studioConfigurationFrom(Long wspId, StudioConfigurationOptions configurationOptions, String condaEnvOverride) throws ApiException {
        return studioConfigurationFrom(wspId, null, configurationOptions, condaEnvOverride);
    }
    protected DataStudioConfiguration studioConfigurationFrom(Long wspId, DataStudioDto baseStudio, StudioConfigurationOptions configurationOptions) throws ApiException {
        return studioConfigurationFrom(wspId, baseStudio, configurationOptions, null);
    }
    protected DataStudioConfiguration studioConfigurationFrom(Long wspId, DataStudioDto baseStudio, StudioConfigurationOptions configOptions, String condaEnvOverride) throws ApiException {
        DataStudioConfiguration studioConfiguration = baseStudio == null || baseStudio.getConfiguration() == null
                ? new DataStudioConfiguration()
                : baseStudio.getConfiguration();

        studioConfiguration.setGpu(configOptions.gpu == null
                ? studioConfiguration.getGpu()
                : configOptions.gpu);
        studioConfiguration.setCpu(configOptions.cpu == null
                ? studioConfiguration.getCpu()
                : configOptions.cpu);
        studioConfiguration.setMemory(configOptions.memory == null
                ? studioConfiguration.getMemory()
                : configOptions.memory);

        studioConfiguration.setMountData(getMountDataIds(configOptions, studioConfiguration, wspId));


        if (condaEnvOverride != null && !condaEnvOverride.isEmpty()) {
            studioConfiguration.setCondaEnvironment(condaEnvOverride);
        }

        return studioConfiguration;
    }

    List<String> getMountDataIds(StudioConfigurationOptions studioConfigOptions, DataStudioConfiguration currentStudioConfiguration, Long wspId) throws ApiException {
        if (studioConfigOptions.dataLinkRefOptions == null || studioConfigOptions.dataLinkRefOptions.dataLinkRef == null) {
            return currentStudioConfiguration.getMountData();
        }

        DataLinkService dataLinkService = new DataLinkService(api(), app());
        return dataLinkService.getDataLinkIds(studioConfigOptions.dataLinkRefOptions.dataLinkRef, wspId);
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
                DataStudioDto studioDto = api().describeDataStudio(sessionId, workspaceId);

                Optional<DataStudioProgressStep> inProgressStep = studioDto.getProgress().stream()
                        .filter(step -> step.getStatus() == IN_PROGRESS || step.getStatus() == ERRORED)
                        .findFirst();

                if (inProgressStep.isPresent() && !inProgressStep.get().equals(currentProgressStep)) {
                    currentProgressStep = inProgressStep.get();
                    return currentProgressStep.getStatus() != ERRORED
                            ? String.format("\n  %s", currentProgressStep.getMessage())
                            : String.format("\n  %s - Error encountered: %s", currentProgressStep.getMessage(), studioDto.getStatusInfo().getMessage());
                }

                return "";
            } catch (Exception e) {
                return "";
            }
        }
    }
}
