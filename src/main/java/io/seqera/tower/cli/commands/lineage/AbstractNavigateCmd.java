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
import io.seqera.tower.cli.responses.lineage.LineageRecordsList;
import io.seqera.tower.model.SearchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command
public abstract class AbstractNavigateCmd extends AbstractLineageCmd {

    public enum RecordType {FileOutput, TaskRun}

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @Mixin
    public LidOptions lidOptions;

    @Option(names = {"-t", "--type"}, description = "Show only records of this type (${COMPLETION-CANDIDATES})")
    public RecordType type;

    protected abstract String direction();

    protected abstract SearchResult navigate(String lid, Long wspId, String type) throws ApiException;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        SearchResult result = navigate(lidPathSegment(lidOptions.lid), wspId, type == null ? null : type.name());
        return new LineageRecordsList(String.format("Lineage records %s of '%s'", direction(), lidOptions.lid), result.getRecords(), result.getNextPageToken());
    }
}
