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

package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.InfoResponse;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.DescribeUserResponse;
import io.seqera.tower.model.ServiceInfoResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@CommandLine.Command(
        name = "info",
        description = "System info and health status."
)
public class InfoCmd extends AbstractRootCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        int connectionCheck = 1;
        int versionCheck = -1;
        int credentialsCheck = -1;

        String userName = null;
        String towerVersion = null;
        String cliVersion = null;
        ModuleDescriptor.Version towerApiVersion = null;
        ModuleDescriptor.Version cliApiVersion = null;

        // Cli parameters
        Properties props = getCliProperties();
        cliApiVersion = ModuleDescriptor.Version.parse(props.get("versionApi").toString());
        cliVersion = String.format("%s (%s)", props.get("version"), props.get("commitId"));

        try {
            ServiceInfoResponse infoResponse = api().info();

            if (infoResponse.getServiceInfo() != null && infoResponse.getServiceInfo().getApiVersion() != null) {
                towerApiVersion = ModuleDescriptor.Version.parse(infoResponse.getServiceInfo().getApiVersion());
                towerVersion = infoResponse.getServiceInfo().getVersion();

                versionCheck = towerApiVersion.compareTo(cliApiVersion) >= 0 ? 1 : 0;
            }
        } catch (TowerException e) {
            throw e;
        } catch (Exception e) {
            connectionCheck = 0;
        }

        if (connectionCheck == 1) {
            try {
                DescribeUserResponse resp = api().userInfo();
                userName = resp.getUser().getUserName();
                credentialsCheck = 1;
            } catch (ApiException apiException) {
                if (apiException.getCode() == 401) {
                    credentialsCheck = 0;
                }
            }
        }

        Map<String, String> opts = new HashMap<>();
        opts.put("cliApiVersion", cliApiVersion != null ? cliApiVersion.toString() : null);
        opts.put("cliVersion", cliVersion);
        opts.put("towerApiVersion", towerApiVersion != null ? towerApiVersion.toString() : null);
        opts.put("towerApiEndpoint", this.apiUrl());
        opts.put("towerVersion", towerVersion);
        opts.put("userName", userName);

        return new InfoResponse(connectionCheck, versionCheck, credentialsCheck, opts);
    }
}
