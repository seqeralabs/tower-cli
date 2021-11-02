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

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.ComputeEnvs.ComputeEnvExport;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Export compute environment for further creation"
)
public class ExportCmd extends AbstractComputeEnvCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Compute environment name", required = true)
    public String name;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to export", arity = "0..1")
    String fileName = null;

    @Override
    protected Response exec() throws ApiException {
        ComputeEnv ce = findComputeEnvironmentByName(name, workspaceId());

        ComputeEnv computeEnv = new ComputeEnv();
        computeEnv.setDescription(ce.getDescription());
        computeEnv.setCredentialsId(ce.getCredentialsId());
        computeEnv.setMessage(ce.getMessage());
        computeEnv.setPlatform(ce.getPlatform());
        computeEnv.setConfig(ce.getConfig());

        return new ComputeEnvExport(computeEnv.getConfig(), fileName);
    }
}
