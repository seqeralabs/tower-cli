package io.seqera.tower.cli.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.computeenv.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.AwsBatchManualPlatform;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;

import java.io.IOException;

public class ComputeEnvView extends Response {

    private String id;
    private ComputeEnv computeEnv;
    private String workspaceRef;
    private boolean showConfig;

    public ComputeEnvView(String id, String workspaceRef, ComputeEnv computeEnv, boolean showConfig) {
        this.id = id;
        this.computeEnv = computeEnv;
        this.workspaceRef = workspaceRef;
        this.showConfig = showConfig;
    }

    @Override
    public Object getBody() {
        return computeEnv;
    }

    @Override
    public String toString() {
        String configJson = "";

        if (this.showConfig) {

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
        }

        return String.format("Compute environment '%s' at %s workspace.%n%s", id, workspaceRef, configJson);
    }
}
