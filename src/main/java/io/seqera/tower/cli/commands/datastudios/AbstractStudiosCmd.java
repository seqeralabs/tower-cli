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

import java.util.Optional;
import java.util.function.Supplier;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioProgressStep;

import static io.seqera.tower.model.DataStudioProgressStepStatus.ERRORED;
import static io.seqera.tower.model.DataStudioProgressStepStatus.IN_PROGRESS;

public class AbstractStudiosCmd extends AbstractApiCmd {

    protected DataStudioDto fetchDataStudio(DataStudioRefOptions dataStudioRefOptions, Long wspId) throws ApiException {
        return api().describeDataStudio(dataStudioRefOptions.dataStudio.sessionId, wspId);
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

                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
