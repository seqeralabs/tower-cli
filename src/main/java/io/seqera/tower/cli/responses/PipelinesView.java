package io.seqera.tower.cli.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.computeenv.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.utils.ModelHelper;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;

public class PipelinesView extends Response {

    private String workspaceRef;
    private PipelineDbDto info;
    private Launch launch;
    private boolean showConfig;

    public PipelinesView(String workspaceRef, PipelineDbDto info, Launch launch, boolean showConfig) {
        this.workspaceRef = workspaceRef;
        this.info = info;
        this.launch = launch;
        this.showConfig = showConfig;
    }

    @Override
    public String toString() {
        String configJson = "";

        if (this.showConfig) {

            try {
                WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(launch);
                configJson = new JSON().getContext(WorkflowLaunchRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(request);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return String.format("Pipeline '%s' at %s workspace.%n%s", info.getName(), workspaceRef, configJson);
    }
}
