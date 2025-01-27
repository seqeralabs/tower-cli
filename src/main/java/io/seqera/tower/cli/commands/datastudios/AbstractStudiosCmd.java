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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
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

public class AbstractStudiosCmd extends AbstractApiCmd {

    protected DataStudioDto fetchDataStudio(DataStudioRefOptions dataStudioRefOptions, Long wspId) throws ApiException {
        return api().describeDataStudio(dataStudioRefOptions.dataStudio.sessionId, wspId);
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
