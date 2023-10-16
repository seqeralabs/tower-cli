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

package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.labels.LabelsSubcmdOptions;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collections;

@CommandLine.Command(name = "labels", description = "Manages labels for actions.")
public class LabelsCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    public ActionRefOptions actionRefOptions;

    @CommandLine.Mixin
    public LabelsSubcmdOptions labelsSubcmdOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(labelsSubcmdOptions.workspace.workspace);
        DefaultApi api = api();
        String actionId = fetchDescribeActionResponse(actionRefOptions, wspId).getAction().getId();
        ActionsLabelsManager creator = new ActionsLabelsManager(api);
        return creator.execute(wspId, actionId, labelsSubcmdOptions);
    }
}
