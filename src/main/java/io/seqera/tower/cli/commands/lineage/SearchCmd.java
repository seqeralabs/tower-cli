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


package io.seqera.tower.cli.commands.lineage;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.lineage.LineageRecordsList;
import io.seqera.tower.model.SearchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "search",
        description = "Search lineage records across all the workspaces you can access"
)
public class SearchCmd extends AbstractLineageCmd {

    @Option(names = {"-q", "--query"}, description = "Search query. Combines free text and the qualifiers: `type`, `workflow`, `task`, `pipeline`, `pipelineId`, `label`, and `workspace` (org/name) or `workspaceId` to narrow the scope. Whitespace means AND, a comma inside a qualifier means OR. Example: -q 'type:FileOutput workspace:acme/dev multiqc'.")
    public String query;

    @Option(names = {"--max"}, description = "Maximum number of records per page (capped by the server)")
    public Integer max;

    @Option(names = {"--page-token"}, description = "Token of the page to show, as printed by a previous search with the same query")
    public String pageToken;

    @Override
    protected Response exec() throws ApiException {
        SearchResult result = lineageApi().searchLineage(query, pageToken, max);
        return new LineageRecordsList("Lineage records", result.getRecords(), result.getNextPageToken());
    }
}
