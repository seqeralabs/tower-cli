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

package io.seqera.tower.cli.datastudios;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.DataStudiosCustomTemplateWithCondaException;
import io.seqera.tower.cli.exceptions.DataStudiosTemplateNotFoundException;
import io.seqera.tower.cli.exceptions.MultipleDataLinksFoundException;
import io.seqera.tower.cli.responses.datastudios.DataStudioCheckpointsList;
import io.seqera.tower.cli.responses.datastudios.DataStudioDeleted;
import io.seqera.tower.cli.responses.datastudios.DataStudioStartSubmitted;
import io.seqera.tower.cli.responses.datastudios.DataStudiosCreated;
import io.seqera.tower.cli.responses.datastudios.DataStudioStopSubmitted;
import io.seqera.tower.cli.responses.datastudios.DataStudiosList;
import io.seqera.tower.cli.responses.datastudios.DataStudiosTemplatesList;
import io.seqera.tower.cli.responses.datastudios.DataStudiosView;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.DataStudioCheckpointDto;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioTemplatesListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class DataStudiosCmdTest extends BaseCmdTest {

    @BeforeEach
    void init(MockServerClient mock) {
        mock.reset();
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "view", "-w", "75887156211589", "-i" ,"3e8370e7");

        assertOutput(format, out, new DataStudiosView(parseJson("""
                {
                  "sessionId": "3e8370e7",
                  "workspaceId": 75887156211589,
                  "user": {
                    "id": 2345,
                    "userName": "John Doe",
                    "email": "john@seqera.io",
                    "avatar": null
                  },
                  "name": "studio-a66d",
                  "description": "my first studio",
                  "studioUrl": "https://a3e8370e7.dev-tower.com",
                  "computeEnv": {
                    "id": "3xkkzYH2nbD3nZjrzKm0oR",
                    "name": "ce1",
                    "platform": "aws-batch",
                    "region": "us-east-2"
                  },
                  "template": {
                    "repository": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                    "icon": "vscode"
                  },
                  "configuration": {
                    "gpu": 0,
                    "cpu": 2,
                    "memory": 8192,
                    "mountData": [
                      "v1-user-1ccf131810375d303bf0402dd8423433"
                    ],
                    "condaEnvironment":null
                  },
                  "dateCreated": "2024-12-19T06:49:24.893122+01:00",
                  "lastUpdated": "2024-12-19T06:52:50.686822+01:00",
                  "statusInfo": {
                    "status": "running",
                    "message": "",
                    "lastUpdate": "2024-12-19T05:52:41.823Z"
                  },
                  "waveBuildUrl": null,
                  "baseImage": "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                  "mountedDataLinks": [
                    {
                      "id": "v1-user-1ccf131810375d303bf0402dd8423433",
                      "name": "aaa-my-bucket",
                      "resourceRef": "s3://aaa-my-bucket",
                      "type": "bucket",
                      "provider": "aws",
                      "region": "us-east-2"
                    }
                  ],
                  "progress": [
                    {
                      "status": "succeeded",
                      "message": "Provisioning compute resources",
                      "warnings": null
                    },
                    {
                      "status": "succeeded",
                      "message": "Mounting checkpoints",
                      "warnings": null
                    }
                  ]
                }""", DataStudioDto.class), "[organization1 / workspace1]" ));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "list", "-w", "75887156211589");

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Arrays.asList(parseJson("""
                         {
                                    "sessionId": "ddfd5e14",
                                    "workspaceId": 75887156211589,
                                    "parentCheckpoint": null,
                                    "user": {
                                        "id": 1,
                                        "userName": "samurai-jack",
                                        "email": "jack@seqera.io",
                                        "avatar": null
                                    },
                                    "name": "studio-7728",
                                    "description": "Local studio",
                                    "studioUrl": "http://addfd5e14.studio.localhost:9191",
                                    "computeEnv": {
                                        "id": "16esMgELkyQ3QPcHGNTiXQ",
                                        "name": "my-other-local-ce",
                                        "platform": "local-platform",
                                        "region": null
                                    },
                                    "template": {
                                        "repository": "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                                        "icon": "jupyter"
                                    },
                                    "configuration": {
                                        "gpu": 0,
                                        "cpu": 2,
                                        "memory": 8192,
                                        "mountData": [],
                                        "condaEnvironment": null
                                    },
                                    "dateCreated": "2025-01-14T11:51:05.393498Z",
                                    "lastUpdated": "2025-01-15T09:10:30.016752Z",
                                    "activeConnections": [],
                                    "statusInfo": {
                                        "status": "running",
                                        "message": "",
                                        "lastUpdate": "2025-01-15T09:10:30.016588Z"
                                    },
                                    "waveBuildUrl": null,
                                    "baseImage": null,
                                    "customImage": false,
                                    "progress": null
                                }\
                        """, DataStudioDto.class),
                parseJson("""
                        {
                                    "sessionId": "3e8370e7",
                                    "workspaceId": 75887156211589,
                                    "parentCheckpoint": null,
                                    "user": {
                                        "id": 1,
                                        "userName": "johnny-bravo",
                                        "email": "johnny@seqera.io",
                                        "avatar": null
                                    },
                                    "name": "studio-a66d",
                                    "description": "my first studio",
                                    "studioUrl": "http://a3e8370e7.studio.localhost:9191",
                                    "computeEnv": {
                                        "id": "61DYXYj3XQAYbJIHrI1XSg",
                                        "name": "my-local-ce",
                                        "platform": "local-platform",
                                        "region": null
                                    },
                                    "template": {
                                        "repository": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                                        "icon": "vscode"
                                    },
                                    "configuration": {
                                        "gpu": 0,
                                        "cpu": 2,
                                        "memory": 8192,
                                        "mountData": [
                                             "v1-user-1ccf131810375d303bf0402dd8423433"
                                           ],
                                        "condaEnvironment": null
                                    },
                                    "dateCreated": "2025-01-10T17:26:36.83703Z",
                                    "lastUpdated": "2025-01-12T03:00:30.014415Z",
                                    "activeConnections": [],
                                    "statusInfo": {
                                        "status": "errored",
                                        "message": "",
                                        "lastUpdate": "2025-01-12T03:00:30.010738Z"
                                    },
                                    "waveBuildUrl": null,
                                    "baseImage": null,
                                    "customImage": false,
                                    "progress": null
                                }""", DataStudioDto.class)
        ), null));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListWithOffset(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("offset", "1")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "list", "-w", "75887156211589", "--offset", "1", "--max", "2");

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Arrays.asList(parseJson("""
                         {
                                    "sessionId": "ddfd5e14",
                                    "workspaceId": 75887156211589,
                                    "parentCheckpoint": null,
                                    "user": {
                                        "id": 1,
                                        "userName": "samurai-jack",
                                        "email": "jack@seqera.io",
                                        "avatar": null
                                    },
                                    "name": "studio-7728",
                                    "description": "Local studio",
                                    "studioUrl": "http://addfd5e14.studio.localhost:9191",
                                    "computeEnv": {
                                        "id": "16esMgELkyQ3QPcHGNTiXQ",
                                        "name": "my-other-local-ce",
                                        "platform": "local-platform",
                                        "region": null
                                    },
                                    "template": {
                                        "repository": "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                                        "icon": "jupyter"
                                    },
                                    "configuration": {
                                        "gpu": 0,
                                        "cpu": 2,
                                        "memory": 8192,
                                        "mountData": [],
                                        "condaEnvironment": null
                                    },
                                    "dateCreated": "2025-01-14T11:51:05.393498Z",
                                    "lastUpdated": "2025-01-15T09:10:30.016752Z",
                                    "activeConnections": [],
                                    "statusInfo": {
                                        "status": "running",
                                        "message": "",
                                        "lastUpdate": "2025-01-15T09:10:30.016588Z"
                                    },
                                    "waveBuildUrl": null,
                                    "baseImage": null,
                                    "customImage": false,
                                    "progress": null
                                }\
                        """, DataStudioDto.class),
                parseJson("""
                        {
                                    "sessionId": "3e8370e7",
                                    "workspaceId": 75887156211589,
                                    "parentCheckpoint": null,
                                    "user": {
                                        "id": 1,
                                        "userName": "johnny-bravo",
                                        "email": "johnny@seqera.io",
                                        "avatar": null
                                    },
                                    "name": "studio-a66d",
                                    "description": "my first studio",
                                    "studioUrl": "http://a3e8370e7.studio.localhost:9191",
                                    "computeEnv": {
                                        "id": "61DYXYj3XQAYbJIHrI1XSg",
                                        "name": "my-local-ce",
                                        "platform": "local-platform",
                                        "region": null
                                    },
                                    "template": {
                                        "repository": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                                        "icon": "vscode"
                                    },
                                    "configuration": {
                                        "gpu": 0,
                                        "cpu": 2,
                                        "memory": 8192,
                                        "mountData": [
                                             "v1-user-1ccf131810375d303bf0402dd8423433"
                                           ],
                                        "condaEnvironment": null
                                    },
                                    "dateCreated": "2025-01-10T17:26:36.83703Z",
                                    "lastUpdated": "2025-01-12T03:00:30.014415Z",
                                    "activeConnections": [],
                                    "statusInfo": {
                                        "status": "errored",
                                        "message": "",
                                        "lastUpdate": "2025-01-12T03:00:30.010738Z"
                                    },
                                    "waveBuildUrl": null,
                                    "baseImage": null,
                                    "customImage": false,
                                    "progress": null
                                }""", DataStudioDto.class)
        ), PaginationInfo.from(1, 2, null, 2L)));
    }


    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListWithPage(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_list_filtered_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "list", "-w", "75887156211589", "--page", "1", "--max", "1");

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Collections.singletonList(parseJson("""
                 {
                            "sessionId": "ddfd5e14",
                            "workspaceId": 75887156211589,
                            "parentCheckpoint": null,
                            "user": {
                                "id": 1,
                                "userName": "samurai-jack",
                                "email": "jack@seqera.io",
                                "avatar": null
                            },
                            "name": "studio-7728",
                            "description": "Local studio",
                            "studioUrl": "http://addfd5e14.studio.localhost:9191",
                            "computeEnv": {
                                "id": "16esMgELkyQ3QPcHGNTiXQ",
                                "name": "my-other-local-ce",
                                "platform": "local-platform",
                                "region": null
                            },
                            "template": {
                                "repository": "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                                "icon": "jupyter"
                            },
                            "configuration": {
                                "gpu": 0,
                                "cpu": 2,
                                "memory": 8192,
                                "mountData": [],
                                "condaEnvironment": null
                            },
                            "dateCreated": "2025-01-14T11:51:05.393498Z",
                            "lastUpdated": "2025-01-15T09:10:30.016752Z",
                            "activeConnections": [],
                            "statusInfo": {
                                "status": "running",
                                "message": "",
                                "lastUpdate": "2025-01-15T09:10:30.016588Z"
                            },
                            "waveBuildUrl": null,
                            "baseImage": null,
                            "customImage": false,
                            "progress": null
                        }\
                """, DataStudioDto.class)
        ), PaginationInfo.from(null, 1, 1, 2L)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListWithFilter(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("search", "status:running"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_list_filtered_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "list", "-w", "75887156211589", "--filter", "status:running");

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Collections.singletonList(parseJson("""
                 {
                            "sessionId": "ddfd5e14",
                            "workspaceId": 75887156211589,
                            "parentCheckpoint": null,
                            "user": {
                                "id": 1,
                                "userName": "samurai-jack",
                                "email": "jack@seqera.io",
                                "avatar": null
                            },
                            "name": "studio-7728",
                            "description": "Local studio",
                            "studioUrl": "http://addfd5e14.studio.localhost:9191",
                            "computeEnv": {
                                "id": "16esMgELkyQ3QPcHGNTiXQ",
                                "name": "my-other-local-ce",
                                "platform": "local-platform",
                                "region": null
                            },
                            "template": {
                                "repository": "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                                "icon": "jupyter"
                            },
                            "configuration": {
                                "gpu": 0,
                                "cpu": 2,
                                "memory": 8192,
                                "mountData": [],
                                "condaEnvironment": null
                            },
                            "dateCreated": "2025-01-14T11:51:05.393498Z",
                            "lastUpdated": "2025-01-15T09:10:30.016752Z",
                            "activeConnections": [],
                            "statusInfo": {
                                "status": "running",
                                "message": "",
                                "lastUpdate": "2025-01-15T09:10:30.016588Z"
                            },
                            "waveBuildUrl": null,
                            "baseImage": null,
                            "customImage": false,
                            "progress": null
                        }\
                """, DataStudioDto.class)
        ), null));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStart(OutputType format, MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/studios/3e8370e7/start").withQueryStringParameter("workspaceId", "75887156211589").withBody(json("""
                           {
                             "configuration": {
                               "gpu": 0,
                               "cpu": 2,
                               "memory": 8192,
                               "mountData": [
                                 "v1-user-1ccf131810375d303bf0402dd8423433"
                               ]
                             },
                             "description": "my first studio"
                           }
                           """
                        )

                ), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_start_response")).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(format, mock, "studios", "start", "-w", "75887156211589", "-i" ,"3e8370e7");

        assertOutput(format, out, new DataStudioStartSubmitted("3e8370e7", "3e8370e7",75887156211589L,
                "[organization1 / workspace1]",  "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", true));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStartByName(OutputType format, MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/studios/3e8370e7/start").withQueryStringParameter("workspaceId", "75887156211589").withBody(json("""
                           {
                             "configuration": {
                               "gpu": 0,
                               "cpu": 2,
                               "memory": 8192,
                               "mountData": [
                                 "v1-user-1ccf131810375d303bf0402dd8423433"
                               ]
                             },
                             "description": "my first studio"
                           }
                           """
                        )

                ), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_start_response")).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(format, mock, "studios", "start", "-w", "organization1/workspace1", "-n" ,"studio-a66d");

        assertOutput(format, out, new DataStudioStartSubmitted("3e8370e7","studio-a66d", 75887156211589L,
                "[organization1 / workspace1]",  "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", true));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStartWithConfigOverride(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/studios/3e8370e7/start").withQueryStringParameter("workspaceId", "75887156211589").withBody(json("""
                           {
                             "configuration": {
                               "gpu": 0,
                               "cpu": 4,
                               "memory": 8192,
                               "mountData": [
                                 "v1-user-1ccf131810375d303bf0402dd8423433"
                               ]
                             },
                             "description": "Override description"
                           }
                           """
                        )

                ), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_start_response")).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(format, mock, "studios", "start", "-w", "75887156211589", "-i" ,"3e8370e7", "--cpu", "4", "--description", "Override description");

        assertOutput(format, out, new DataStudioStartSubmitted("3e8370e7", "3e8370e7",75887156211589L,
                "[organization1 / workspace1]", "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", true));
    }


    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStartWithMountDateByName(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        // status check
        mock.when(
                request()
                        .withMethod("GET").withPath("/data-links")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "1"),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("data/links/datalinks_list")).withContentType(MediaType.APPLICATION_JSON)
        );
        // mock fetch data links
        mock.when(
                request().withMethod("GET").withPath("/data-links")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("search", "a-test-bucket-eend-us-east-1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("data/links/datalinks_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/studios/3e8370e7/start").withQueryStringParameter("workspaceId", "75887156211589").withBody(json("""
                           {
                             "configuration": {
                               "gpu": 0,
                               "cpu": 2,
                               "memory": 8192,
                               "mountData": [
                                 "v1-cloud-c2875f38a7b5c8fe34a5b382b5f9e0c4"
                               ]
                             },
                             "description": "my first studio"
                           }
                           """
                        )

                ), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_start_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "start", "-w", "75887156211589", "-i" ,"3e8370e7", "--mount-data", "a-test-bucket-eend-us-east-1");

        assertOutput(format, out, new DataStudioStartSubmitted("3e8370e7", "3e8370e7",75887156211589L,
                "[organization1 / workspace1]",  "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", true));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStartMultipleDataLinksFoundThrowsError(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        // status check
        mock.when(
                request()
                        .withMethod("GET").withPath("/data-links")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "1"),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("data/links/datalinks_list")).withContentType(MediaType.APPLICATION_JSON)
        );
        // mock fetch data links
        mock.when(
                request().withMethod("GET").withPath("/data-links")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("search", "resourceRef:s3://a-test-bucket"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(json("""
            {
              "dataLinks": [
                {
                  "id": "v1-cloud-id-aaa",
                  "name": "a-test-bucket-name",
                  "resourceRef": "s3://a-test-bucket"
                },
                {
                  "id": "v1-cloud-id-bbb",
                  "name": "a-different-bucket",
                  "resourceRef": "s3://a-test-bucket"
                }
              ]
            }
            """)).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "start", "-w", "75887156211589", "-i" ,"3e8370e7", "--mount-data-resource-refs", "s3://a-test-bucket");

        assertEquals(errorMessage(out.app, new MultipleDataLinksFoundException("resourceRef:s3://a-test-bucket", 75887156211589L, List.of("v1-cloud-id-aaa", "v1-cloud-id-bbb"))), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    // Only run this test in json output format, since extra stdout output is printed out to console with --wait flag
    @ParameterizedTest
    @EnumSource(value = OutputType.class, names = {"json"})
    void testStartWithWait(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/studios/3e8370e7/start").withQueryStringParameter("workspaceId", "75887156211589").withBody(json("""
                           {
                             "configuration": {
                               "gpu": 0,
                               "cpu": 2,
                               "memory": 8192,
                               "mountData": [
                                 "v1-user-1ccf131810375d303bf0402dd8423433"
                               ]
                             },
                             "description": "my first studio"
                           }
                           """
                        )

                ), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_start_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(2)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_starting")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "start", "-w", "75887156211589", "-i" ,"3e8370e7", "--wait", "RUNNING");

        assertOutput(format, out, new DataStudioStartSubmitted("3e8370e7", "3e8370e7",75887156211589L,
                "[organization1 / workspace1]", "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", true));

        // verify the API has been polled additionally for the status
        mock.verify(request().withMethod("GET").withPath("/studios/3e8370e7"), VerificationTimes.exactly(4));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testTemplates(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/templates")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("max", "20"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_templates_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "templates", "-w", "75887156211589");

        assertOutput(format, out, new DataStudiosTemplatesList(parseJson("""
                {
                  "templates": [
                    {
                      "repository": "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                      "icon": "jupyter"
                    },
                    {
                      "repository": "cr.seqera.io/public/data-studio-rstudio:4.4.1-u1-snapshot",
                      "icon": "rstudio"
                    },
                    {
                      "repository": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                      "icon": "vscode"
                    },
                    {
                      "repository": "cr.seqera.io/public/data-studio-xpra:6.2.0-r2-1-snapshot",
                      "icon": "xpra"
                    }
                  ],
                  "totalSize": 4
                }""", DataStudioTemplatesListResponse.class).getTemplates())
        );
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock){

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/templates")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("max", "20"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_templates_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("autostart", "false"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_created_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "add", "-n", "studio-a66d", "-w", "75887156211589", "-t" ,"cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot", "-c", "7WgvfmcjAwp3Or75UphCJl");

        assertOutput(format, out, new DataStudiosCreated("3e8370e7",75887156211589L, "[organization1 / workspace1]",
                "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", false));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddThrowsDataStudiosTemplateNotFoundException(OutputType format, MockServerClient mock){

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("autostart", "false"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_created_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/templates")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("max", "20"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_templates_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "add", "-n", "studio-a66d", "-w", "75887156211589", "-t" ,"invalid-template-vs-code", "-c", "7WgvfmcjAwp3Or75UphCJl");

        List<String> availableTemplate = List.of(
                "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                "cr.seqera.io/public/data-studio-rstudio:4.4.1-u1-snapshot",
                "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                "cr.seqera.io/public/data-studio-xpra:6.2.0-r2-1-snapshot");

        assertEquals(errorMessage(out.app, new DataStudiosTemplateNotFoundException("invalid-template-vs-code", availableTemplate)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddThrowsDataStudiosCustomTemplateWithCondaException(OutputType format, MockServerClient mock){

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("autostart", "false"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_created_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "add", "-n", "studio-a66d", "-w", "75887156211589", "-ct" ,"custom-template", "--conda-env-yml", "path/to/any/yaml/file","-c", "7WgvfmcjAwp3Or75UphCJl");

        assertEquals(errorMessage(out.app, new DataStudiosCustomTemplateWithCondaException()), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    // Only run this test in json output format, since extra stdout output is printed out to console with --wait flag
    @ParameterizedTest
    @EnumSource(value = OutputType.class, names = {"json"})
    void testAddWithAutoStartAndWait(OutputType format, MockServerClient mock){

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("autostart", "true"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_created_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_starting")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/templates")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("max", "20"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_templates_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "add", "-n", "studio-a66d", "-w", "75887156211589", "-t" ,"cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                "-c", "7WgvfmcjAwp3Or75UphCJl", "-a", "--wait", "running");

        assertOutput(format, out, new DataStudiosCreated("3e8370e7",75887156211589L, "[organization1 / workspace1]",
                "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", true));

        // verify the API has been polled additionally for the status
        mock.verify(request().withMethod("GET").withPath("/studios/3e8370e7"), VerificationTimes.exactly(2));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStartAsNew(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(2)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(2)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7/checkpoints")
                        .withQueryStringParameter("workspaceId","75887156211589")
                        .withQueryStringParameter("max","1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"checkpoints\":[],\"totalSize\":0}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("autostart", "false"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_start_as_new_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "start-as-new", "-pid", "3e8370e7", "-n", "child-studio-a66d", "-w", "75887156211589", "--cpu", "4");

        assertOutput(format, out, new DataStudiosCreated("8aebf1b8",75887156211589L, "[organization1 / workspace1]",
                "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", false));


        mock.when(
                request().withMethod("GET").withPath("/studios/8aebf1b8").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_start_as_new_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut outView = exec(format, mock, "studios", "view", "-i", "8aebf1b8", "-w", "75887156211589");

        assertOutput(format, outView, new DataStudiosView(parseJson("""
                {
                  "sessionId": "8aebf1b8",
                  "workspaceId": 75887156211589,
                  "user": {
                    "id": 2345,
                    "userName": "John Doe",
                    "email": "john@seqera.io",
                    "avatar": null
                  },
                  "name": "studio-a66d",
                  "description": "Started from studio studio-a66d",
                  "studioUrl": "https://a8aebf1b8.dev-tower.com",
                  "computeEnv": {
                    "id": "3xkkzYH2nbD3nZjrzKm0oR",
                    "name": "ce1",
                    "platform": "aws-batch",
                    "region": "us-east-2"
                  },
                  "template": {
                    "repository": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                    "icon": "vscode"
                  },
                  "configuration": {
                    "gpu": 0,
                    "cpu": 4,
                    "memory": 8192,
                    "mountData": [],
                    "condaEnvironment":null
                  },
                  "dateCreated": "2025-01-28T10:44:44.833455494Z",
                  "lastUpdated": "2025-01-28T10:44:44.833455494Z",
                  "statusInfo": {
                    "status": "stopped",
                    "message": "",
                    "lastUpdate": "2025-01-28T10:44:44.650457005Z"
                  },
                  "waveBuildUrl": null,
                  "baseImage": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                  "mountedDataLinks": [],
                  "progress": []
                }
                """, DataStudioDto.class), "[organization1 / workspace1]"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStartAsNewUsingParentName(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(2)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(2)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_view_response_studio_stopped")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7/checkpoints")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("max", "1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"checkpoints\":[],\"totalSize\":0}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/studios")
                        .withQueryStringParameter("workspaceId", "75887156211589")
                        .withQueryStringParameter("autostart", "false"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_start_as_new_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "start-as-new", "-pn", "studio-a66d", "-n", "child-studio-a66d", "-w", "75887156211589", "--cpu", "4");

        assertOutput(format, out, new DataStudiosCreated("8aebf1b8", 75887156211589L, "[organization1 / workspace1]",
                "http://localhost:"+mock.getPort()+"/orgs/organization1/workspaces/workspace1", false));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStop(OutputType format, MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/studios/3e8370e7/stop").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(json("""
                        {
                          "jobSubmitted": true,
                          "sessionId": "3e8370e7",
                          "statusInfo": {
                            "status": "stopping",
                            "message": "",
                            "lastUpdate": "2025-01-22T15:16:11.508692Z"
                          }
                        }
                        """)).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "stop", "-w", "75887156211589", "-i" ,"3e8370e7");

        assertOutput(format, out, new DataStudioStopSubmitted("3e8370e7", "3e8370e7",75887156211589L,
                "[organization1 / workspace1]", true));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testStopByName(OutputType format, MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/studios/3e8370e7/stop").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(json("""
                        {
                          "jobSubmitted": true,
                          "sessionId": "3e8370e7",
                          "statusInfo": {
                            "status": "stopping",
                            "message": "",
                            "lastUpdate": "2025-01-22T15:16:11.508692Z"
                          }
                        }
                        """)).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "stop", "-w", "75887156211589", "-n" ,"studio-a66d");

        assertOutput(format, out, new DataStudioStopSubmitted("3e8370e7", "studio-a66d",75887156211589L,
                "[organization1 / workspace1]", true));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/studios/3e8370e7").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(format, mock, "studios", "delete", "-w", "75887156211589", "-i" ,"3e8370e7");

        assertOutput(format, out, new DataStudioDeleted("3e8370e7", "[organization1 / workspace1]"));
    }


    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testCheckpoints(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/studios/3e8370e7/checkpoints").withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datastudios/datastudios_checkpoints_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "studios", "checkpoints", "-w", "75887156211589", "-i", "3e8370e7");

        assertOutput(format, out, new DataStudioCheckpointsList("3e8370e7", "[organization1 / workspace1]", Arrays.asList(parseJson("""
                            {
                               "id": 2,
                               "name": "studio-a66d_2",
                               "dateSaved": "2025-01-30T09:34:33Z",
                               "dateCreated": "2025-01-30T09:29:31Z",
                               "author": {
                                 "id": 100,
                                 "userName": "johnny-bravo"
                               }
                            }
                            """, DataStudioCheckpointDto.class),
                parseJson("""
                            {
                              "id": 1,
                              "name": "studio-a66d_1",
                              "dateSaved": "2025-01-28T14:05:07Z",
                              "dateCreated": "2025-01-28T12:49:06Z",
                              "author": {
                                "id": 100,
                                "userName": "johnny-bravo"
                              }
                            }
                            """, DataStudioCheckpointDto.class)
        ), null));
    }

}
