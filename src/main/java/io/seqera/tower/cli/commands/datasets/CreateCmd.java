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

package io.seqera.tower.cli.commands.datasets;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetCreate;
import io.seqera.tower.model.CreateDatasetRequest;
import io.seqera.tower.model.CreateDatasetResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
        name = "create",
        description = "Create a workspace dataset."
)
public class CreateCmd extends AbstractDatasetsCmd{

    @CommandLine.Option(names = {"-n", "--name"}, description = "Dataset name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Dataset description.")
    public String description;

    @CommandLine.Option(names = {"--header"}, description = "Set first row as a header.")
    public boolean header = false;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "Data file to upload.", arity = "1")
    Path fileName = null;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        CreateDatasetRequest request = new CreateDatasetRequest();
        request.setName(name);
        request.setDescription(description);

        CreateDatasetResponse response = api().createDataset(wspId, request);

        api().uploadDataset(wspId, response.getDataset().getId(), header, fileName.toFile());

        return new DatasetCreate(response.getDataset().getName(), workspace.workspace, response.getDataset().getId());
    }
}
