/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.data.links;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkContentDeleted;
import io.seqera.tower.model.DataLinkDeleteItemRequest;
import io.seqera.tower.model.DataLinkDeleteItemResponse;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;

@CommandLine.Command(
        name = "delete-content",
        description = "Delete files or folders from a data link. Supported for Seqera Compute data links only."
)
public class DeleteContentCmd extends AbstractDataLinksCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataLinkRefOptions dataLinkRefOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier", required = true)
    public String credentialsRef;

    @CommandLine.Option(names = {"--file"}, description = "File path to delete, relative to the data link root. Can be specified multiple times.")
    public List<String> files;

    @CommandLine.Option(names = {"--dir"}, description = "Folder path to delete with all its contents, relative to the data link root. Can be specified multiple times.")
    public List<String> dirs;

    @Override
    protected Response exec() throws ApiException {
        if (files == null && dirs == null) {
            throw new TowerException("Provide at least one --file or --dir to delete");
        }

        Long wspId = workspaceId(workspace.workspace);
        String credId = credentialsByRef(null, wspId, credentialsRef);
        String id = getDataLinkId(dataLinkRefOptions, wspId, credId);

        DataLinkDeleteItemRequest request = new DataLinkDeleteItemRequest().files(files).dirs(dirs);
        try {
            dataLinksApi().deleteDataLinkItem(id, request, wspId, credId);
        } catch (ApiException e) {
            // The platform answers 500 with the list of items it could not delete when the deletion is partial.
            DataLinkDeleteItemResponse failed = e.getCode() == 500 ? parseDeletionFailures(e.getResponseBody()) : null;
            if (failed == null || failed.getDeletionFailures() == null || failed.getDeletionFailures().isEmpty()) {
                throw e;
            }
            String failures = failed.getDeletionFailures().stream()
                    .map(f -> String.format("'%s': %s", f.getDataLinkItem() == null ? "" : f.getDataLinkItem().getName(), f.getErrorMessage()))
                    .collect(Collectors.joining(", "));
            throw new TowerException(String.format("Failed to delete some items from data link '%s': %s", id, failures));
        }

        return new DataLinkContentDeleted(id, wspId, files, dirs);
    }

    private static DataLinkDeleteItemResponse parseDeletionFailures(String body) {
        if (body == null) {
            return null;
        }
        try {
            return parseJson(body, DataLinkDeleteItemResponse.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
