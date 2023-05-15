/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.responses.computeenvs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchManualPlatform;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnvResponseDto;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatComputeEnvId;
import static io.seqera.tower.cli.utils.FormatHelper.formatComputeEnvStatus;
import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;
import static io.seqera.tower.cli.utils.FormatHelper.formatTime;

public class ComputeEnvView extends Response {

    public final String id;
    public final ComputeEnvResponseDto computeEnv;
    public final String workspaceRef;

    @JsonIgnore
    private String baseWorkspaceUrl;

    public ComputeEnvView(String id, String workspaceRef, ComputeEnvResponseDto computeEnv, String baseWorkspaceUrl) {
        this.id = id;
        this.computeEnv = computeEnv;
        this.workspaceRef = workspaceRef;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public Object getJSON() {
        return computeEnv;
    }

    @Override
    public void toString(PrintWriter out) {
        String configJson = "";

        // Remove forged resources
        ComputeConfig config = computeEnv.getConfig();
        if (config instanceof AwsBatchConfig) {
            AwsBatchConfig awsCfg = (AwsBatchConfig) config;
            if (awsCfg.getForge() != null) {
                AwsBatchForgePlatform.clean(awsCfg);
            } else {
                AwsBatchManualPlatform.clean(awsCfg);
            }
        }

        try {
            configJson = new JSON().getContext(ComputeConfig.class).writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        out.println(ansi(String.format("%n  @|bold Compute environment at %s workspace:|@%n", workspaceRef)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", formatComputeEnvId(id, baseWorkspaceUrl));
        table.addRow("Name", computeEnv.getName());
        table.addRow("Platform", computeEnv.getPlatform().getValue());
        table.addRow("Last updated", formatTime(computeEnv.getLastUpdated()));
        table.addRow("Last activity", formatTime(computeEnv.getLastUsed()));
        table.addRow("Created", formatTime(computeEnv.getDateCreated()));
        table.addRow("Status", formatComputeEnvStatus(computeEnv.getStatus()));
        table.addRow("Labels", formatLabels(computeEnv.getLabels()));
        table.print();

        out.println(String.format("%n  Configuration:%n%n%s%n", configJson.replaceAll("(?m)^", "     ")));
    }
}
