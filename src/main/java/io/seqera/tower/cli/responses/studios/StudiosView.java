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

package io.seqera.tower.cli.responses.studios;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataLinkDto;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioStatusInfo;
import io.seqera.tower.model.StudioUser;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatDescription;
import static io.seqera.tower.cli.utils.FormatHelper.formatStudioStatus;
import static io.seqera.tower.cli.utils.FormatHelper.formatTime;

public class StudiosView extends Response {

    public final DataStudioDto studio;
    public final String workspaceRef;

    public StudiosView(DataStudioDto dataStudioDto, String workspaceRef) {
        this.studio = dataStudioDto;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public void toString(PrintWriter out){
        out.println(ansi(String.format("%n  @|bold Studio at workspace '%s'|@%n", workspaceRef)));

        DataStudioStatusInfo statusInfo = studio.getStatusInfo();
        StudioUser studioUser = studio.getUser();
        DataStudioConfiguration config = studio.getConfiguration();
        TableList table = new TableList(out, 2);
        table.setPrefix(" ");
        table.addRow("SessionID", studio.getSessionId());
        table.addRow("Name", studio.getName());
        table.addRow("Status", formatStudioStatus(statusInfo == null ? null : statusInfo.getStatus()));
        table.addRow("Status last update", statusInfo == null ? "NA" : formatTime(statusInfo.getLastUpdate()));
        table.addRow("Studio URL", studio.getStudioUrl());
        table.addRow("Description", formatDescription(studio.getDescription(), 100));
        table.addRow("Created on", formatTime(studio.getDateCreated()));
        table.addRow("Created by", studioUser == null ? "NA" : String.format("%s | %s",studioUser.getUserName(), studioUser.getEmail()));
        table.addRow("Template", studio.getTemplate() == null ? "NA" : studio.getTemplate().getRepository());
        table.addRow("Mounted data", studio.getMountedDataLinks() == null ? "NA" : studio.getMountedDataLinks()
                .stream().map(DataLinkDto::getResourceRef).collect(java.util.stream.Collectors.joining(", ")));
        table.addRow("Compute environment", studio.getComputeEnv() == null ? "NA" : studio.getComputeEnv().getName());
        table.addRow("Region", studio.getComputeEnv() == null ? "NA" : studio.getComputeEnv().getRegion());
        table.addRow("GPU allocated",  config == null ? "-" : String.valueOf(config.getGpu()));
        table.addRow("CPU allocated",  config == null ? "-" : String.valueOf(config.getCpu()));
        table.addRow("Memory allocated", config == null ? "-" : String.valueOf(config.getMemory()));
        table.addRow("Build reports", studio.getWaveBuildUrl() == null ? "NA" : studio.getWaveBuildUrl());

        table.print();
        if (config != null && config.getCondaEnvironment() != null && !config.getCondaEnvironment().isEmpty()) {
            out.println(String.format("%n  Conda Environment:%n%n%s%n", config.getCondaEnvironment().replaceAll("(?m)^", "     ")));
        }

    }
}
