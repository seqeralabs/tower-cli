package io.seqera.tower.cli.responses.ComputeEnvs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.CreateComputeEnvRequest;

public class ComputeEnvExport extends Response {

    public final CreateComputeEnvRequest request;
    public final String fileName;

    public ComputeEnvExport(CreateComputeEnvRequest request, String fileName) {
        this.request = request;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        String configOutput = "";

        try {
            configOutput = new JSON().getContext(CreateComputeEnvRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (fileName != null) {
            FilesHelper.saveString(fileName, configOutput);
        }

        return configOutput;
    }
}
