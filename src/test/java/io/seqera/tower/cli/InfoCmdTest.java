/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli;

import io.seqera.tower.cli.responses.InfoResponse;
import org.junit.jupiter.api.Test;
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

    @Test
    void testInfo(MockServerClient mock) throws IOException {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("info/service-info")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", "1.6");
        opts.put("towerApiVersion", "1.6.0");
        opts.put("towerVersion", "21.10.0");
        opts.put("towerApiEndpoint", "http://localhost:"+mock.getPort());
        opts.put("userName", "jordi");

        ExecOut out = exec(mock, "info");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(chop(new InfoResponse(1,1,1, opts).toString()), out.stdOut);
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
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(401)
        );

        ExecOut out = exec(mock, "info");

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", "1.6");
        opts.put("towerApiVersion", "1.6.0");
        opts.put("towerVersion", "21.10.0");
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
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "info");

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", "1.6");
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
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withContentType(MediaType.HTML_UTF_8)
        );

        ExecOut out = exec(mock, "info");

        Map<String, String> opts = new HashMap<>();
        opts.put("cliVersion", getCliVersion() );
        opts.put("cliApiVersion", "1.6");
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
}
