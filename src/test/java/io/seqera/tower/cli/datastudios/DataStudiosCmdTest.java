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
import io.seqera.tower.cli.responses.datastudios.DataStudiosList;
import io.seqera.tower.cli.responses.datastudios.DataStudiosView;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.DataStudioDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Collections;

import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class DataStudiosCmdTest extends BaseCmdTest {

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

        assertOutput(format, out, new DataStudiosView(parseJson("{\n" +
                "  \"sessionId\": \"3e8370e7\",\n" +
                "  \"workspaceId\": 75887156211589,\n" +
                "  \"user\": {\n" +
                "    \"id\": 2345,\n" +
                "    \"userName\": \"John Doe\",\n" +
                "    \"email\": \"john@seqera.io\",\n" +
                "    \"avatar\": null\n" +
                "  },\n" +
                "  \"name\": \"studio-a66d\",\n" +
                "  \"description\": \"my first studio\",\n" +
                "  \"studioUrl\": \"https://a3e8370e7.dev-tower.com\",\n" +
                "  \"computeEnv\": {\n" +
                "    \"id\": \"3xkkzYH2nbD3nZjrzKm0oR\",\n" +
                "    \"name\": \"ce1\",\n" +
                "    \"platform\": \"aws-batch\",\n" +
                "    \"region\": \"us-east-2\"\n" +
                "  },\n" +
                "  \"template\": {\n" +
                "    \"repository\": \"cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot\",\n" +
                "    \"icon\": \"vscode\"\n" +
                "  },\n" +
                "  \"configuration\": {\n" +
                "    \"gpu\": 0,\n" +
                "    \"cpu\": 2,\n" +
                "    \"memory\": 8192,\n" +
                "    \"mountData\": [\n" +
                "      \"v1-user-1ccf131810375d303bf0402dd8423433\"\n" +
                "    ],\n" +
                "    \"condaEnvironment\":null\n" +
                "  },\n" +
                "  \"dateCreated\": \"2024-12-19T06:49:24.893122+01:00\",\n" +
                "  \"lastUpdated\": \"2024-12-19T06:52:50.686822+01:00\",\n" +
                "  \"statusInfo\": {\n" +
                "    \"status\": \"running\",\n" +
                "    \"message\": \"\",\n" +
                "    \"lastUpdate\": \"2024-12-19T05:52:41.823Z\"\n" +
                "  },\n" +
                "  \"waveBuildUrl\": null,\n" +
                "  \"baseImage\": \"cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot\",\n" +
                "  \"mountedDataLinks\": [\n" +
                "    {\n" +
                "      \"id\": \"v1-user-1ccf131810375d303bf0402dd8423433\",\n" +
                "      \"name\": \"aaa-my-bucket\",\n" +
                "      \"resourceRef\": \"s3://aaa-my-bucket\",\n" +
                "      \"type\": \"bucket\",\n" +
                "      \"provider\": \"aws\",\n" +
                "      \"region\": \"us-east-2\"\n" +
                "    }\n" +
                "  ]\n" +
                "}", DataStudioDto.class), "[organization1 / workspace1]" ));
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

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Arrays.asList(parseJson(" {\n" +
                        "            \"sessionId\": \"ddfd5e14\",\n" +
                        "            \"workspaceId\": 75887156211589,\n" +
                        "            \"parentCheckpoint\": null,\n" +
                        "            \"user\": {\n" +
                        "                \"id\": 1,\n" +
                        "                \"userName\": \"samurai-jack\",\n" +
                        "                \"email\": \"jack@seqera.io\",\n" +
                        "                \"avatar\": null\n" +
                        "            },\n" +
                        "            \"name\": \"studio-7728\",\n" +
                        "            \"description\": \"Local studio\",\n" +
                        "            \"studioUrl\": \"http://addfd5e14.studio.localhost:9191\",\n" +
                        "            \"computeEnv\": {\n" +
                        "                \"id\": \"16esMgELkyQ3QPcHGNTiXQ\",\n" +
                        "                \"name\": \"my-other-local-ce\",\n" +
                        "                \"platform\": \"local-platform\",\n" +
                        "                \"region\": null\n" +
                        "            },\n" +
                        "            \"template\": {\n" +
                        "                \"repository\": \"cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot\",\n" +
                        "                \"icon\": \"jupyter\"\n" +
                        "            },\n" +
                        "            \"configuration\": {\n" +
                        "                \"gpu\": 0,\n" +
                        "                \"cpu\": 2,\n" +
                        "                \"memory\": 8192,\n" +
                        "                \"mountData\": [],\n" +
                        "                \"condaEnvironment\": null\n" +
                        "            },\n" +
                        "            \"dateCreated\": \"2025-01-14T11:51:05.393498Z\",\n" +
                        "            \"lastUpdated\": \"2025-01-15T09:10:30.016752Z\",\n" +
                        "            \"activeConnections\": [],\n" +
                        "            \"statusInfo\": {\n" +
                        "                \"status\": \"running\",\n" +
                        "                \"message\": \"\",\n" +
                        "                \"lastUpdate\": \"2025-01-15T09:10:30.016588Z\"\n" +
                        "            },\n" +
                        "            \"waveBuildUrl\": null,\n" +
                        "            \"baseImage\": null,\n" +
                        "            \"customImage\": false,\n" +
                        "            \"progress\": null\n" +
                        "        }", DataStudioDto.class),
                parseJson("{\n" +
                        "            \"sessionId\": \"c779bf09\",\n" +
                        "            \"workspaceId\": 75887156211589,\n" +
                        "            \"parentCheckpoint\": null,\n" +
                        "            \"user\": {\n" +
                        "                \"id\": 1,\n" +
                        "                \"userName\": \"johnny-bravo\",\n" +
                        "                \"email\": \"johnny@seqera.io\",\n" +
                        "                \"avatar\": null\n" +
                        "            },\n" +
                        "            \"name\": \"studio-d456\",\n" +
                        "            \"description\": null,\n" +
                        "            \"studioUrl\": \"http://ac779bf09.studio.localhost:9191\",\n" +
                        "            \"computeEnv\": {\n" +
                        "                \"id\": \"61DYXYj3XQAYbJIHrI1XSg\",\n" +
                        "                \"name\": \"my-local-ce\",\n" +
                        "                \"platform\": \"local-platform\",\n" +
                        "                \"region\": null\n" +
                        "            },\n" +
                        "            \"template\": {\n" +
                        "                \"repository\": \"cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot\",\n" +
                        "                \"icon\": \"vscode\"\n" +
                        "            },\n" +
                        "            \"configuration\": {\n" +
                        "                \"gpu\": 0,\n" +
                        "                \"cpu\": 2,\n" +
                        "                \"memory\": 8192,\n" +
                        "                \"mountData\": [],\n" +
                        "                \"condaEnvironment\": null\n" +
                        "            },\n" +
                        "            \"dateCreated\": \"2025-01-10T17:26:36.83703Z\",\n" +
                        "            \"lastUpdated\": \"2025-01-12T03:00:30.014415Z\",\n" +
                        "            \"activeConnections\": [],\n" +
                        "            \"statusInfo\": {\n" +
                        "                \"status\": \"errored\",\n" +
                        "                \"message\": \"\",\n" +
                        "                \"lastUpdate\": \"2025-01-12T03:00:30.010738Z\"\n" +
                        "            },\n" +
                        "            \"waveBuildUrl\": null,\n" +
                        "            \"baseImage\": null,\n" +
                        "            \"customImage\": false,\n" +
                        "            \"progress\": null\n" +
                        "        }", DataStudioDto.class)
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

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Arrays.asList(parseJson(" {\n" +
                        "            \"sessionId\": \"ddfd5e14\",\n" +
                        "            \"workspaceId\": 75887156211589,\n" +
                        "            \"parentCheckpoint\": null,\n" +
                        "            \"user\": {\n" +
                        "                \"id\": 1,\n" +
                        "                \"userName\": \"samurai-jack\",\n" +
                        "                \"email\": \"jack@seqera.io\",\n" +
                        "                \"avatar\": null\n" +
                        "            },\n" +
                        "            \"name\": \"studio-7728\",\n" +
                        "            \"description\": \"Local studio\",\n" +
                        "            \"studioUrl\": \"http://addfd5e14.studio.localhost:9191\",\n" +
                        "            \"computeEnv\": {\n" +
                        "                \"id\": \"16esMgELkyQ3QPcHGNTiXQ\",\n" +
                        "                \"name\": \"my-other-local-ce\",\n" +
                        "                \"platform\": \"local-platform\",\n" +
                        "                \"region\": null\n" +
                        "            },\n" +
                        "            \"template\": {\n" +
                        "                \"repository\": \"cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot\",\n" +
                        "                \"icon\": \"jupyter\"\n" +
                        "            },\n" +
                        "            \"configuration\": {\n" +
                        "                \"gpu\": 0,\n" +
                        "                \"cpu\": 2,\n" +
                        "                \"memory\": 8192,\n" +
                        "                \"mountData\": [],\n" +
                        "                \"condaEnvironment\": null\n" +
                        "            },\n" +
                        "            \"dateCreated\": \"2025-01-14T11:51:05.393498Z\",\n" +
                        "            \"lastUpdated\": \"2025-01-15T09:10:30.016752Z\",\n" +
                        "            \"activeConnections\": [],\n" +
                        "            \"statusInfo\": {\n" +
                        "                \"status\": \"running\",\n" +
                        "                \"message\": \"\",\n" +
                        "                \"lastUpdate\": \"2025-01-15T09:10:30.016588Z\"\n" +
                        "            },\n" +
                        "            \"waveBuildUrl\": null,\n" +
                        "            \"baseImage\": null,\n" +
                        "            \"customImage\": false,\n" +
                        "            \"progress\": null\n" +
                        "        }", DataStudioDto.class),
                parseJson("{\n" +
                        "            \"sessionId\": \"c779bf09\",\n" +
                        "            \"workspaceId\": 75887156211589,\n" +
                        "            \"parentCheckpoint\": null,\n" +
                        "            \"user\": {\n" +
                        "                \"id\": 1,\n" +
                        "                \"userName\": \"johnny-bravo\",\n" +
                        "                \"email\": \"johnny@seqera.io\",\n" +
                        "                \"avatar\": null\n" +
                        "            },\n" +
                        "            \"name\": \"studio-d456\",\n" +
                        "            \"description\": null,\n" +
                        "            \"studioUrl\": \"http://ac779bf09.studio.localhost:9191\",\n" +
                        "            \"computeEnv\": {\n" +
                        "                \"id\": \"61DYXYj3XQAYbJIHrI1XSg\",\n" +
                        "                \"name\": \"my-local-ce\",\n" +
                        "                \"platform\": \"local-platform\",\n" +
                        "                \"region\": null\n" +
                        "            },\n" +
                        "            \"template\": {\n" +
                        "                \"repository\": \"cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot\",\n" +
                        "                \"icon\": \"vscode\"\n" +
                        "            },\n" +
                        "            \"configuration\": {\n" +
                        "                \"gpu\": 0,\n" +
                        "                \"cpu\": 2,\n" +
                        "                \"memory\": 8192,\n" +
                        "                \"mountData\": [],\n" +
                        "                \"condaEnvironment\": null\n" +
                        "            },\n" +
                        "            \"dateCreated\": \"2025-01-10T17:26:36.83703Z\",\n" +
                        "            \"lastUpdated\": \"2025-01-12T03:00:30.014415Z\",\n" +
                        "            \"activeConnections\": [],\n" +
                        "            \"statusInfo\": {\n" +
                        "                \"status\": \"errored\",\n" +
                        "                \"message\": \"\",\n" +
                        "                \"lastUpdate\": \"2025-01-12T03:00:30.010738Z\"\n" +
                        "            },\n" +
                        "            \"waveBuildUrl\": null,\n" +
                        "            \"baseImage\": null,\n" +
                        "            \"customImage\": false,\n" +
                        "            \"progress\": null\n" +
                        "        }", DataStudioDto.class)
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

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Arrays.asList(parseJson(" {\n" +
                        "            \"sessionId\": \"ddfd5e14\",\n" +
                        "            \"workspaceId\": 75887156211589,\n" +
                        "            \"parentCheckpoint\": null,\n" +
                        "            \"user\": {\n" +
                        "                \"id\": 1,\n" +
                        "                \"userName\": \"samurai-jack\",\n" +
                        "                \"email\": \"jack@seqera.io\",\n" +
                        "                \"avatar\": null\n" +
                        "            },\n" +
                        "            \"name\": \"studio-7728\",\n" +
                        "            \"description\": \"Local studio\",\n" +
                        "            \"studioUrl\": \"http://addfd5e14.studio.localhost:9191\",\n" +
                        "            \"computeEnv\": {\n" +
                        "                \"id\": \"16esMgELkyQ3QPcHGNTiXQ\",\n" +
                        "                \"name\": \"my-other-local-ce\",\n" +
                        "                \"platform\": \"local-platform\",\n" +
                        "                \"region\": null\n" +
                        "            },\n" +
                        "            \"template\": {\n" +
                        "                \"repository\": \"cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot\",\n" +
                        "                \"icon\": \"jupyter\"\n" +
                        "            },\n" +
                        "            \"configuration\": {\n" +
                        "                \"gpu\": 0,\n" +
                        "                \"cpu\": 2,\n" +
                        "                \"memory\": 8192,\n" +
                        "                \"mountData\": [],\n" +
                        "                \"condaEnvironment\": null\n" +
                        "            },\n" +
                        "            \"dateCreated\": \"2025-01-14T11:51:05.393498Z\",\n" +
                        "            \"lastUpdated\": \"2025-01-15T09:10:30.016752Z\",\n" +
                        "            \"activeConnections\": [],\n" +
                        "            \"statusInfo\": {\n" +
                        "                \"status\": \"running\",\n" +
                        "                \"message\": \"\",\n" +
                        "                \"lastUpdate\": \"2025-01-15T09:10:30.016588Z\"\n" +
                        "            },\n" +
                        "            \"waveBuildUrl\": null,\n" +
                        "            \"baseImage\": null,\n" +
                        "            \"customImage\": false,\n" +
                        "            \"progress\": null\n" +
                        "        }", DataStudioDto.class)
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

        assertOutput(format, out, new DataStudiosList("[organization1 / workspace1]", Collections.singletonList(parseJson(" {\n" +
                "            \"sessionId\": \"ddfd5e14\",\n" +
                "            \"workspaceId\": 75887156211589,\n" +
                "            \"parentCheckpoint\": null,\n" +
                "            \"user\": {\n" +
                "                \"id\": 1,\n" +
                "                \"userName\": \"samurai-jack\",\n" +
                "                \"email\": \"jack@seqera.io\",\n" +
                "                \"avatar\": null\n" +
                "            },\n" +
                "            \"name\": \"studio-7728\",\n" +
                "            \"description\": \"Local studio\",\n" +
                "            \"studioUrl\": \"http://addfd5e14.studio.localhost:9191\",\n" +
                "            \"computeEnv\": {\n" +
                "                \"id\": \"16esMgELkyQ3QPcHGNTiXQ\",\n" +
                "                \"name\": \"my-other-local-ce\",\n" +
                "                \"platform\": \"local-platform\",\n" +
                "                \"region\": null\n" +
                "            },\n" +
                "            \"template\": {\n" +
                "                \"repository\": \"cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot\",\n" +
                "                \"icon\": \"jupyter\"\n" +
                "            },\n" +
                "            \"configuration\": {\n" +
                "                \"gpu\": 0,\n" +
                "                \"cpu\": 2,\n" +
                "                \"memory\": 8192,\n" +
                "                \"mountData\": [],\n" +
                "                \"condaEnvironment\": null\n" +
                "            },\n" +
                "            \"dateCreated\": \"2025-01-14T11:51:05.393498Z\",\n" +
                "            \"lastUpdated\": \"2025-01-15T09:10:30.016752Z\",\n" +
                "            \"activeConnections\": [],\n" +
                "            \"statusInfo\": {\n" +
                "                \"status\": \"running\",\n" +
                "                \"message\": \"\",\n" +
                "                \"lastUpdate\": \"2025-01-15T09:10:30.016588Z\"\n" +
                "            },\n" +
                "            \"waveBuildUrl\": null,\n" +
                "            \"baseImage\": null,\n" +
                "            \"customImage\": false,\n" +
                "            \"progress\": null\n" +
                "        }", DataStudioDto.class)
        ), null));
    }
}
