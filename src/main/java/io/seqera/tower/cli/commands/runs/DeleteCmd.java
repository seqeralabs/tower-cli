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

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunDeleted;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a pipeline's execution"
)
public class DeleteCmd extends AbstractRunsCmd {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline's run identifier", required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException, IOException {
        try {
            api().deleteWorkflow(id, workspaceId());

            return new RunDeleted(id, workspaceRef());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                throw new RunNotFoundException(id, workspaceRef());
            }
            throw e;
        }
    }
}
