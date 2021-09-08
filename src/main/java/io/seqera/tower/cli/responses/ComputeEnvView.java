package io.seqera.tower.cli.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchManualPlatform;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;

import java.io.PrintWriter;

public class ComputeEnvView extends Response {

    public final String id;
    public final ComputeEnv computeEnv;
    public final String workspaceRef;

    public ComputeEnvView(String id, String workspaceRef, ComputeEnv computeEnv) {
        this.id = id;
        this.computeEnv = computeEnv;
        this.workspaceRef = workspaceRef;
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
        table.addRow("ID", id);
        table.addRow("Name", computeEnv.getName());
        table.addRow("Platform", computeEnv.getPlatform().getValue());
        table.addRow("Last updated", formatTime(computeEnv.getLastUpdated()));
        table.addRow("Last activity", formatTime(computeEnv.getLastUsed()));
        table.addRow("Created", formatTime(computeEnv.getDateCreated()));
        table.print();

        out.println(String.format("%n  Configuration:%n%n%s%n", configJson.replaceAll("(?m)^", "     ")));
    }
}
