package io.seqera.tower.cli.responses.pipelines;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class PipelinesExport extends Response {

    public final PipelineDbDto pipeline;
    public final Launch launch;
    public final String format;
    public final String fileName;

    public PipelinesExport(PipelineDbDto pipeline, Launch launch, String format, String fileName) {
        this.pipeline = pipeline;
        this.launch = launch;
        this.format = format;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        String configOutput = "";

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

        try {
            if (Objects.equals(format, "yml")) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
                configOutput = mapper.writeValueAsString(createPipelineRequest);
            } else {
                configOutput = new JSON().getContext(CreatePipelineRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(createPipelineRequest);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (fileName != null) {
            try {
                BufferedWriter writer;
                writer = new BufferedWriter(new FileWriter(fileName));
                writer.write(configOutput);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return configOutput;
    }
}
