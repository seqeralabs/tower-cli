/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.responses.pipelines;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineDbDto;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatPipelineId;
import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;

public class PipelinesList extends Response {

    public final String workspaceRef;
    public final List<PipelineDbDto> pipelines;

    @JsonIgnore
    private final String baseWorkspaceUrl;

    @JsonIgnore
    private boolean includeLabels;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public PipelinesList(String workspaceRef, List<PipelineDbDto> pipelines, String baseWorkspaceUrl, boolean includeLabels, @Nullable PaginationInfo paginationInfo) {
        this.workspaceRef = workspaceRef;
        this.pipelines = pipelines;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.includeLabels = includeLabels;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Pipelines at %s workspace:|@%n", workspaceRef)));

        if (pipelines.isEmpty()) {
            out.println(ansi("    @|yellow No pipelines found|@"));
            return;
        }

        List<String> descriptions = new ArrayList<>(List.of("ID", "Name", "Repository", "Visibility"));
        if (includeLabels) descriptions.add("Labels");

        TableList table = new TableList(out, descriptions.size(), descriptions.toArray(new String[descriptions.size()])).sortBy(0);
        table.setPrefix("    ");
        pipelines.forEach(pipe -> {

            List<String> rows = new ArrayList<>(List.of(
                    formatPipelineId(pipe.getPipelineId(), baseWorkspaceUrl),
                    pipe.getName(),
                    pipe.getRepository(),
                    pipe.getVisibility() == null ? "" : pipe.getVisibility()
            ));
            if (includeLabels) rows.add(formatLabels(pipe.getLabels()));

            table.addRow(rows.toArray(new String[rows.size()]));
        });

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }

}
