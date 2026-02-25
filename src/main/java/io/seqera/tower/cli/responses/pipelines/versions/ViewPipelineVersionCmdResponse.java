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
 *
 */

package io.seqera.tower.cli.responses.pipelines.versions;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineVersionFullInfoDto;

import java.io.PrintWriter;

public class ViewPipelineVersionCmdResponse extends Response {

    public final String workspaceRef;
    public final Long pipelineId;
    public final String pipelineName;
    public final PipelineVersionFullInfoDto version;

    public ViewPipelineVersionCmdResponse(String workspaceRef, Long pipelineId, String pipelineName, PipelineVersionFullInfoDto version) {
        this.workspaceRef = workspaceRef;
        this.pipelineId = pipelineId;
        this.pipelineName = pipelineName;
        this.version = version;
    }

    @Override
    public void toString(PrintWriter out) {

        if (workspaceRef != null) {
            out.println(ansi(String.format("%n  @|bold Pipeline version of '%s' in workspace %s :|@%n", pipelineName, workspaceRef)));
        } else {
            out.println(ansi(String.format("%n  @|bold Pipeline version of '%s' in user workspace:|@%n", pipelineName)));
        }

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", version.getId());
        table.addRow("Name", version.getName() != null ? version.getName() : "(draft)");
        table.addRow("Is Default", version.getIsDefault() != null && version.getIsDefault() ? "yes" : "no");
        table.addRow("Hash", version.getHash());
        table.addRow("Creator", version.getCreatorUserName());
        table.addRow("Created At", FormatHelper.formatTime(version.getDateCreated()));
        table.addRow("Last Updated", FormatHelper.formatTime(version.getLastUpdated()));
        table.print();

        out.println();
    }
}
