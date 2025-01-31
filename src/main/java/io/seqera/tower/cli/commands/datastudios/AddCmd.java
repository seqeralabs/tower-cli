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

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.DataStudiosCustomTemplateWithCondaException;
import io.seqera.tower.cli.exceptions.DataStudiosTemplateNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datastudios.DataStudiosCreated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.model.DataStudioCreateRequest;
import io.seqera.tower.model.DataStudioCreateResponse;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioStatus;
import io.seqera.tower.model.DataStudioTemplate;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "add",
        description = "Add new data studio."
)
public class AddCmd extends AbstractStudiosCmd{

    @CommandLine.Option(names = {"-n", "--name"}, description = "Data Studio name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Data studio description.")
    public String description;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataStudioTemplateOptions templateOptions;

    @CommandLine.Option(names = {"--conda-env-yml", "--conda-env-yaml"}, description = "Path to a YAML env file with Conda packages to be installed in the Data Studio environment.")
    public Path condaEnv;

    @CommandLine.Option(names = {"-c", "--compute-env"}, description = "Compute environment name.", required = true)
    public String computeEnv;

    @CommandLine.Mixin
    public DataStudioConfigurationOptions dataStudioConfigOptions;

    @CommandLine.Option(names = {"-a", "--autoStart"}, description = "Create Data Studio and start it immediately, defaults to false", defaultValue = "false")
    public Boolean autoStart;

    @CommandLine.Option(names = {"--wait"}, description = "Wait until DataStudio is in RUNNING status. Valid options: ${COMPLETION-CANDIDATES}.")
    public DataStudioStatus wait;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        try {
            templateValidation(templateOptions, condaEnv, wspId);
            DataStudioCreateRequest request = prepareRequest();
            DataStudioCreateResponse response = api().createDataStudio(request, wspId, autoStart);
            DataStudioDto dataStudioDto = response.getStudio();
            assert dataStudioDto != null;
            return new DataStudiosCreated(dataStudioDto.getSessionId(), wspId, workspaceRef(wspId), baseWorkspaceUrl(wspId), autoStart);
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                throw new TowerException(String.format("User not entitled to create studio at %s workspace", wspId));
            }
            throw e;
        }
    }

    private void templateValidation(DataStudioTemplateOptions templateOptions, Path condaEnv, Long wspId) throws ApiException {
        if (templateOptions.template.standardTemplate != null) {
            checkIfTemplateIsAvailable(templateOptions.template.standardTemplate, wspId);
        } else if (condaEnv != null) {
            throw new DataStudiosCustomTemplateWithCondaException();
        }
    }

    void checkIfTemplateIsAvailable(String template, Long workspaceId) throws ApiException {
        List<DataStudioTemplate> availableTemplates = fetchDataStudioTemplates(workspaceId);
        boolean validTemplate = availableTemplates.stream()
                .anyMatch(t -> t.getRepository() != null && t.getRepository().equals(template));

        if (!validTemplate) {
            throw new DataStudiosTemplateNotFoundException(template, availableTemplates.stream().map(DataStudioTemplate::getRepository).collect(Collectors.toList()));
        }
    }

    DataStudioCreateRequest prepareRequest() throws ApiException {
        DataStudioCreateRequest request = new DataStudioCreateRequest();
        request.setName(name);
        if (description != null && !description.isEmpty()) {request.description(description);}
        request.setDataStudioToolUrl(templateOptions.getTemplate());
        request.setComputeEnvId(computeEnv);

        String condaEnvString = null;
        if (condaEnv != null) {
            try {
                condaEnvString = FilesHelper.readString(condaEnv);
            } catch (IOException e) {
                throw new TowerException(String.format("Cannot read conda environment file: %s. %s", condaEnv, e));
            }
        }


        DataStudioConfiguration newConfig = dataStudioConfigurationFrom(workspaceId(workspace.workspace), dataStudioConfigOptions, condaEnvString);
        request.setConfiguration(setDefaults(newConfig));
        return request;
    }

    DataStudioConfiguration setDefaults(DataStudioConfiguration config) {
        if (config.getGpu() == null) {
            config.setGpu(0);
        }
        if (config.getCpu() == null) {
            config.setCpu(2);
        }
        if (config.getMemory() == null) {
            config.setMemory(8192);
        }
        return config;
    }

    @Override
    protected Integer onBeforeExit(int exitCode, Response response) {

        if (!autoStart) {
            return exitCode;
        }

        if (exitCode != 0 || wait == null || response == null) {
            return exitCode;
        }

        DataStudiosCreated createdResponse = ((DataStudiosCreated) response);
        return onBeforeExit(exitCode, createdResponse.sessionId, createdResponse.workspaceId, wait);
    }
}
