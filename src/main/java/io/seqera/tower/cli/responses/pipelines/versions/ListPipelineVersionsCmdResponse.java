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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

public class ListPipelineVersionsCmdResponse extends Response {

    public final String workspaceRef;
    public final Long pipelineId;
    public final String pipelineName;
    public final List<PipelineVersionFullInfoDto> versions;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    @JsonIgnore
    private boolean showFullHash;

    public ListPipelineVersionsCmdResponse(String workspaceRef, Long pipelineId, String pipelineName, List<PipelineVersionFullInfoDto> versions, @Nullable PaginationInfo paginationInfo, boolean showFullHash) {
        this.workspaceRef = workspaceRef;
        this.pipelineId = pipelineId;
        this.pipelineName = pipelineName;
        this.versions = versions;
        this.paginationInfo = paginationInfo;
        this.showFullHash = showFullHash;
    }

    @Override
    public void toString(PrintWriter out) {

        if (workspaceRef != null) {
            out.println(ansi(String.format("%n  @|bold Pipeline versions of '%s' in workspace %s :|@%n", pipelineName, workspaceRef)));
        } else {
            out.println(ansi(String.format("%n  @|bold Pipeline versions of '%s' in user workspace:|@%n", pipelineName)));
        }

        if (versions.isEmpty()) {
            out.println(ansi("    @|yellow No pipeline versions found|@"));
            return;
        }

        TableList table = new TableList(out, 6, "ID", "Name", "IsDefault", "Hash", "Creator", "Created At");

        versions.stream()
                .sorted(Comparator.comparing(PipelineVersionFullInfoDto::getDateCreated))
                .forEach(version -> table.addRow(
                        version.getId(),
                        version.getName(),
                        version.getIsDefault() ? "yes" : "no",
                        showFullHash ? version.getHash() : FormatHelper.formatLargeStringWithEllipsis(version.getHash(), 40),
                        version.getCreatorUserName(),
                        FormatHelper.formatTime(version.getDateCreated())
                ));

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println();
    }
}
