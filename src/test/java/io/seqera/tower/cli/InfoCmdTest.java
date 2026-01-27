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

package io.seqera.tower.cli;

import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.InfoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class InfoCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testInfo(OutputType format, MockServerClient mock) throws IOException {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("info/service-info")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", getCliApiVersion());
        opts.put("towerApiVersion", "1.98.0");
        opts.put("towerVersion", "22.3.0-torricelli");
        opts.put("towerApiEndpoint", "http://localhost:"+mock.getPort());
        opts.put("userName", "jordi");

        ExecOut out = exec(format, mock, "info");

        assertOutput(format, out, new InfoResponse(1,1,1, opts));
    }

    @Test
    void testInfoStatusTokenFail(MockServerClient mock) throws IOException {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("info/service-info")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(401)
        );

        ExecOut out = exec(mock, "info");

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", getCliApiVersion());
        opts.put("towerApiVersion", "1.98.0");
        opts.put("towerVersion", "22.3.0-torricelli");
        opts.put("towerApiEndpoint", "http://localhost:"+mock.getPort());
        opts.put("userName", null);

        assertEquals("", out.stdErr);
        assertEquals(1, out.exitCode);
        assertEquals(chop(new InfoResponse(1,1,0, opts).toString()), out.stdOut);
    }

    @Test
    void testInfoVersionFail(MockServerClient mock) throws IOException {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("info/service-info-obsolete")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "info");

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", getCliApiVersion());
        opts.put("towerApiVersion", "0.1");
        opts.put("towerVersion", "21.10.0");
        opts.put("towerApiEndpoint", "http://localhost:"+mock.getPort());
        opts.put("userName", "jordi");

        assertEquals("", out.stdErr);
        assertEquals(1, out.exitCode);
        assertEquals(chop(new InfoResponse(1,0,1, opts).toString()), out.stdOut);
    }

    @Test
    void testInfoStatusUrlFail(MockServerClient mock) throws IOException {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("info/service-info"), exactly(1)
        ).respond(
                response().withContentType(MediaType.HTML_UTF_8)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withContentType(MediaType.HTML_UTF_8)
        );

        ExecOut out = exec(mock, "info");

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", getCliApiVersion());
        opts.put("towerApiVersion", null);
        opts.put("towerVersion", null);
        opts.put("towerApiEndpoint", "http://localhost:"+mock.getPort());
        opts.put("userName", null);

        assertEquals("", out.stdErr);
        assertEquals(1, out.exitCode);
        assertEquals(chop(new InfoResponse(0,-1,-1, opts).toString()), out.stdOut);
    }

    private String getCliVersion() throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getResourceAsStream("/META-INF/build-info.properties"));
        return String.format("%s (%s)", props.get("version"), props.get("commitId"));
    }

    private String getCliApiVersion() throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getResourceAsStream("/META-INF/build-info.properties"));
        return String.format("%s", props.get("versionApi"));
    }
}
