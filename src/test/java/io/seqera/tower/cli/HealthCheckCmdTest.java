package io.seqera.tower.cli;

import io.seqera.tower.cli.responses.HealthCheckResponse;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class HealthCheckCmdTest extends BaseCmdTest {

    @Test
    void testHealthStatus(MockServerClient mock) {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("health/service-info")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "health");

        assertEquals("", out.stdErr);
        assertEquals(chop(new HealthCheckResponse(1,1,1).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testHealthStatusTokenFail(MockServerClient mock) {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("health/service-info")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(401)
        );

        ExecOut out = exec(mock, "health");

        assertEquals("", out.stdErr);
        assertEquals(chop(new HealthCheckResponse(1,1,0).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testHealthVersionFail(MockServerClient mock) {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("health/service-info-obsolete")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "health");

        assertEquals("", out.stdErr);
        assertEquals(chop(new HealthCheckResponse(1,0,1).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testHealthStatusUrlFail(MockServerClient mock) {
        mock.reset();
        mock.when(
                request().withMethod("GET").withPath("health/service-info"), exactly(1)
        ).respond(
                response().withContentType(MediaType.HTML_UTF_8)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withContentType(MediaType.HTML_UTF_8)
        );

        ExecOut out = exec(mock, "health");

        assertEquals("", out.stdErr);
        assertEquals(chop(new HealthCheckResponse(0,-1,-1).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
