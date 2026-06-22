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

package io.seqera.tower.cli.workspaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.workspaces.StudiosSettingsUpdated;
import io.seqera.tower.cli.responses.workspaces.StudiosSettingsView;
import io.seqera.tower.model.DataStudioWorkspaceSettingsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class StudiosSettingsCmdTest extends BaseCmdTest {

    private static final String SETTINGS_PATH = "/orgs/27736513644467/workspaces/75887156211589/settings/studios";

    private void mockWorkspaceResolution(MockServerClient mock) {
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
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mockWorkspaceResolution(mock);

        mock.when(
                request().withMethod("GET").withPath(SETTINGS_PATH), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/settings/studios_settings")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "workspaces", "settings", "studios", "view", "-i", "75887156211589");

        assertOutput(format, out, new StudiosSettingsView("workspace1",
                parseJson(new String(loadResource("workspaces/settings/studios_settings")), DataStudioWorkspaceSettingsResponse.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateMergesUnchangedAttributes(OutputType format, MockServerClient mock) {
        mockWorkspaceResolution(mock);

        mock.when(
                request().withMethod("GET").withPath(SETTINGS_PATH), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/settings/studios_settings")).withContentType(MediaType.APPLICATION_JSON)
        );

        // Only lifespanHours is overridden; the other attributes must be preserved from the current settings.
        mock.when(
                request().withMethod("PUT").withPath(SETTINGS_PATH)
                        .withBody(json("{\"containerRepository\":\"195996028523.dkr.ecr.eu-west-1.amazonaws.com/studios\",\"lifespanHours\":12,\"nameStrategy\":\"tagPrefix\",\"privateStudioByDefault\":false}")),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "workspaces", "settings", "studios", "update", "-i", "75887156211589", "--lifespan-hours", "12");

        assertOutput(format, out, new StudiosSettingsUpdated("workspace1"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateResetContainerRepository(OutputType format, MockServerClient mock) {
        mockWorkspaceResolution(mock);

        mock.when(
                request().withMethod("GET").withPath(SETTINGS_PATH), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/settings/studios_settings")).withContentType(MediaType.APPLICATION_JSON)
        );

        // The reset flag sends an explicit null to clear the container repository.
        mock.when(
                request().withMethod("PUT").withPath(SETTINGS_PATH)
                        .withBody(json("{\"containerRepository\":null}")),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "workspaces", "settings", "studios", "update", "-i", "75887156211589", "--reset-container-repository");

        assertOutput(format, out, new StudiosSettingsUpdated("workspace1"));
    }

    @Test
    void testUpdateWithoutOptionsFails(MockServerClient mock) {
        ExecOut out = exec(mock, "workspaces", "settings", "studios", "update", "-i", "75887156211589");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }
}
