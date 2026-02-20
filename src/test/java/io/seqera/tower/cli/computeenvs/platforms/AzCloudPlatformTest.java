/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.computeenvs.platforms;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvAdded;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class AzCloudPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
        mock.reset();

        // given
        mock.when(
                request()
                        .withMethod("GET")
                        .withPath("/credentials")
                        .withQueryStringParameter("platformId", "azure-cloud"),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"azure\",\"description\":null,\"discriminator\":\"azure\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}")
        );

        mock.when(
                request()
                        .withMethod("POST")
                        .withPath("/compute-envs")
                        .withBody(json("""
                                {
                                    "computeEnv": {
                                        "name": "my-azure-cloud-ce",
                                        "platform": "azure-cloud",
                                        "config": {
                                            "workDir": "az://my-container",
                                            "region": "eastus",
                                            "resourceGroup": "my-resource-group",
                                            "fusion2Enabled": true,
                                            "waveEnabled": true
                                        },
                                        "credentialsId": "6XfOhoztUq6de3Dw3X9LSb"
                                    }
                                }""")),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}")
        );

        // when
        var out = exec(format, mock, "compute-envs", "add", "azure-cloud",
                "--name", "my-azure-cloud-ce",
                "--work-dir", "az://my-container",
                "--region", "eastus",
                "--resource-group", "my-resource-group"
        );

        // then
        var expected = new ComputeEnvAdded("azure-cloud", "isnEDBLvHDAIteOEF44ow", "my-azure-cloud-ce", null, USER_WORKSPACE_NAME);
        assertOutput(format, out, expected);
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) throws IOException {
        mock.reset();

        // given
        mock.when(
                request()
                        .withMethod("GET")
                        .withPath("/credentials")
                        .withQueryStringParameter("platformId", "azure-cloud"),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"azure\",\"description\":null,\"discriminator\":\"azure\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}")
        );

        mock.when(
                request()
                        .withMethod("POST")
                        .withPath("/compute-envs")
                        .withBody(json("""
                                {
                                    "computeEnv": {
                                        "name": "my-azure-cloud-advanced",
                                        "platform": "azure-cloud",
                                        "config": {
                                            "workDir": "az://my-container",
                                            "region": "westeurope",
                                            "resourceGroup": "my-resource-group",
                                            "instanceType": "Standard_D4s_v3",
                                            "fusion2Enabled": true,
                                            "waveEnabled": true,
                                            "subscriptionId": "12345678-1234-1234-1234-123456789012",
                                            "networkId": "/subscriptions/.../virtualNetworks/my-vnet",
                                            "managedIdentityId": "/subscriptions/.../userAssignedIdentities/my-identity",
                                            "managedIdentityClientId": "87654321-4321-4321-4321-210987654321",
                                            "logWorkspaceId": "log-workspace-id",
                                            "logTableName": "my-log-table",
                                            "dataCollectionEndpoint": "https://my-dce.eastus.ingest.monitor.azure.com",
                                            "dataCollectionRuleId": "/subscriptions/.../dataCollectionRules/my-dcr"
                                        },
                                        "credentialsId": "6XfOhoztUq6de3Dw3X9LSb"
                                    }
                                }""")),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}")
        );

        // when
        ExecOut out = exec(mock, "compute-envs", "add", "azure-cloud",
                "-n", "my-azure-cloud-advanced",
                "--work-dir", "az://my-container",
                "-r", "westeurope",
                "--resource-group", "my-resource-group",
                "--instance-type", "Standard_D4s_v3",
                "--subscription-id", "12345678-1234-1234-1234-123456789012",
                "--network-id", "/subscriptions/.../virtualNetworks/my-vnet",
                "--managed-identity-id", "/subscriptions/.../userAssignedIdentities/my-identity",
                "--managed-identity-client-id", "87654321-4321-4321-4321-210987654321",
                "--log-workspace-id", "log-workspace-id",
                "--log-table-name", "my-log-table",
                "--data-collection-endpoint", "https://my-dce.eastus.ingest.monitor.azure.com",
                "--data-collection-rule-id", "/subscriptions/.../dataCollectionRules/my-dcr"
        );

        // then
        var expected = new ComputeEnvAdded("azure-cloud", "isnEDBLvHDAIteOEF44ow", "my-azure-cloud-advanced", null, USER_WORKSPACE_NAME);
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(expected.toString(), out.stdOut);
    }
}
