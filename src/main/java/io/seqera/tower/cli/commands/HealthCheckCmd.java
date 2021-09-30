package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.HealthCheckResponse;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ServiceInfoResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

        ModuleDescriptor.Version systemApiVersion = null;
        ModuleDescriptor.Version requiredApiVersion = null;

        try {
            ServiceInfoResponse infoResponse = api().info();

            if (infoResponse.getServiceInfo() != null && infoResponse.getServiceInfo().getApiVersion() != null) {
                systemApiVersion = ModuleDescriptor.Version.parse(infoResponse.getServiceInfo().getApiVersion());
                requiredApiVersion = ModuleDescriptor.Version.parse(getVersionApi());

                versionCheck = systemApiVersion.compareTo(requiredApiVersion) >= 0 ? 1 : 0;
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

        Map<String, String> opts = new HashMap<>();
        opts.put("requiredApiVersion", requiredApiVersion != null ? requiredApiVersion.toString() : null);
        opts.put("systemApiVersion", systemApiVersion != null ? systemApiVersion.toString() : null);
        opts.put("serverUrl", this.serverUrl());

        return new HealthCheckResponse(connectionCheck, versionCheck, credentialsCheck, opts);
    }

    private String getVersionApi() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/META-INF/build-info.properties"));

        return properties.get("versionApi").toString();
    }
}
