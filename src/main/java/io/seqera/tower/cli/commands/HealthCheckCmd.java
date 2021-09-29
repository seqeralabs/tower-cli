package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.HealthCheckResponse;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ServiceInfoResponse;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "health",
        description = "Checks system health status"
)
public class HealthCheckCmd extends AbstractRootCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        int connectionCheck = 1;
        int versionCheck = -1;
        int credentialsCheck = -1;
        int requiredApiVersion = 160;

        try {
            ServiceInfoResponse infoResponse = api().info();

            if (infoResponse.getServiceInfo() != null && infoResponse.getServiceInfo().getApiVersion() != null) {
                versionCheck = 0;
                if (Integer.parseInt(infoResponse.getServiceInfo().getApiVersion().replace(".", "")) >= requiredApiVersion) {
                    versionCheck = 1;
                }
            }
        } catch (Exception e) {
            connectionCheck = 0;
        }

        if (connectionCheck == 1) {
            try {
                api().user();
                credentialsCheck = 1;
            } catch (ApiException apiException) {
                if (apiException.getCode() == 401) {
                    credentialsCheck = 0;
                }
            }
        }

        return new HealthCheckResponse(connectionCheck, versionCheck, credentialsCheck);
    }
}
