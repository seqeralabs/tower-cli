package io.seqera.tower.cli.responses.pipelines;

import java.io.PrintWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;

public class PipelinesExport extends Response {

    public final PipelineDbDto pipeline;
    public final Launch launch;

    public PipelinesExport(PipelineDbDto pipeline, Launch launch) {
        this.pipeline = pipeline;
        this.launch = launch;
    }

    @Override
    public void toString(PrintWriter out) {
        String configJson = "";

        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest();
        workflowLaunchRequest.setComputeEnvId(launch.getComputeEnv().getId());
        workflowLaunchRequest.setPipeline(launch.getPipeline());
        workflowLaunchRequest.setWorkDir(launch.getWorkDir());
        workflowLaunchRequest.setRevision(launch.getRevision());
        workflowLaunchRequest.setSessionId(launch.getSessionId());
        workflowLaunchRequest.setConfigProfiles(launch.getConfigProfiles());
        workflowLaunchRequest.setConfigText(launch.getConfigText());
        workflowLaunchRequest.setParamsText(launch.getParamsText());
        workflowLaunchRequest.setPreRunScript(launch.getPreRunScript());
        workflowLaunchRequest.setPostRunScript(launch.getPostRunScript());
        workflowLaunchRequest.setMainScript(launch.getMainScript());
        workflowLaunchRequest.setEntryName(launch.getEntryName());
        workflowLaunchRequest.setSchemaName(launch.getSchemaName());
        workflowLaunchRequest.setResume(launch.getResume());
        workflowLaunchRequest.setPullLatest(launch.getPullLatest());
        workflowLaunchRequest.setStubRun(launch.getStubRun());

        CreatePipelineRequest createPipelineRequest = new CreatePipelineRequest();
        createPipelineRequest.setName(pipeline.getName());
        createPipelineRequest.setDescription(pipeline.getDescription());
        createPipelineRequest.setIcon(pipeline.getIcon());
        createPipelineRequest.setLaunch(workflowLaunchRequest);

        try{
            configJson = new JSON().getContext(CreatePipelineRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(createPipelineRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        out.println(String.format("%n  Pipeline export configuration:%n%n%s%n", configJson.replaceAll("(?m)^", "     ")));
    }
}
