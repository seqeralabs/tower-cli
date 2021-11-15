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

package io.seqera.tower.cli.commands.computeenvs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchManualPlatform;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.ComputeEnvs.ComputeEnvExport;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Export compute environment for further creation"
)
public class ExportCmd extends AbstractComputeEnvCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Compute environment name", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to export", arity = "0..1")
    String fileName = null;

    @Override
    protected Response exec() throws ApiException {
        ComputeEnv ce = findComputeEnvironmentByName(workspace.workspaceId, name);

        ComputeEnv computeEnv = new ComputeEnv();
        computeEnv.setDescription(ce.getDescription());
        computeEnv.setCredentialsId(ce.getCredentialsId());
        computeEnv.setMessage(ce.getMessage());
        computeEnv.setPlatform(ce.getPlatform());
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

        String configOutput = "";

        try {
            configOutput = new JSON().getContext(ComputeConfig.class).writerWithDefaultPrettyPrinter().writeValueAsString(computeEnv.getConfig());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (fileName != null && !fileName.equals("-")) {
            FilesHelper.saveString(fileName, configOutput);
        }

        return new ComputeEnvExport(configOutput, fileName);
    }
}
