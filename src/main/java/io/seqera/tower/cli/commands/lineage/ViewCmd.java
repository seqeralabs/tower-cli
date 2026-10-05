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
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.lineage.LineageRecordView;
import io.seqera.tower.model.LineageRecordResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "view",
        description = "View a lineage record"
)
public class ViewCmd extends AbstractLineageCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @Mixin
    public LidOptions lidOptions;

    @Option(names = {"--summary"}, description = "Show only the record summary (name, path or run, and labels), read from the lineage index instead of the full record.")
    public boolean summary;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String lid = lidPathSegment(lidOptions.lid);
        LineageRecordResponse record = summary
                ? lineageApi().getLineageSummary(lid, wspId)
                : lineageApi().getLineageRecord(lid, wspId);
        return new LineageRecordView(workspaceRef(wspId), record);
    }
}
