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

package io.seqera.tower.cli.commands.computeenvs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchManualPlatform;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvExport;
import io.seqera.tower.cli.shared.ComputeEnvExportFormat;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.ComputeEnvResponseDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Export compute environment configuration as a JSON file."
)
public class ExportCmd extends AbstractComputeEnvCmd {

    @CommandLine.Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name and path for the exported compute environment configuration.", arity = "0..1")
    String fileName = null;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        ComputeEnvResponseDto ce = fetchComputeEnv(computeEnvRefOptions, wspId);

        ComputeEnvComputeConfig computeEnv = new ComputeEnvComputeConfig();
        computeEnv.setDescription(ce.getDescription());
        computeEnv.setCredentialsId(ce.getCredentialsId());
        computeEnv.setMessage(ce.getMessage());
        computeEnv.setPlatform(ce.getPlatform() != null ? PlatformEnum.fromValue(ce.getPlatform().getValue()) : null);
        computeEnv.setConfig(ce.getConfig());

        // Remove forged resources
        if (computeEnv.getConfig() instanceof AwsBatchConfig) {
            AwsBatchConfig awsCfg = (AwsBatchConfig) computeEnv.getConfig();
            if (awsCfg.getForge() != null) {
                AwsBatchForgePlatform.clean(awsCfg);
            } else {
                AwsBatchManualPlatform.clean(awsCfg);
            }
        }

        ComputeEnvExportFormat ceData = new ComputeEnvExportFormat(
                ce.getConfig(),
                ce.getLabels()
        );

        String configOutput = "";
        try {
            configOutput = ComputeEnvExportFormat.serialize(ceData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (fileName != null && !fileName.equals("-")) {
            FilesHelper.saveString(fileName, configOutput);
        }

        return new ComputeEnvExport(configOutput, fileName);
    }


}
