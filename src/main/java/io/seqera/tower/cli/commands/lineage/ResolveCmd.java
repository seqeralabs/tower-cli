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
import io.seqera.tower.cli.responses.lineage.LineageResolved;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "resolve",
        description = "Resolve a pipeline run or a file path to its lineage record identifier (LID)"
)
public class ResolveCmd extends AbstractLineageCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @ArgGroup(multiplicity = "1")
    public Source source;

    public static class Source {

        @ArgGroup(exclusive = false)
        public Run run;

        @Option(names = {"--file-path"}, description = "Absolute path of a file produced by a pipeline run")
        public String filePath;
    }

    // A resumed run shares the session ID of its parent, so the run name is needed to pick one run
    public static class Run {

        @Option(names = {"--session-id"}, description = "Nextflow session ID of the run", required = true)
        public String sessionId;

        @Option(names = {"--run-name"}, description = "Nextflow run name", required = true)
        public String runName;
    }

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String sessionId = source.run != null ? source.run.sessionId : null;
        String runName = source.run != null ? source.run.runName : null;

        String lid = lineageApi().resolveLineage(wspId, sessionId, runName, source.filePath).getLid();
        return new LineageResolved(workspaceRef(wspId), lid);
    }
}
