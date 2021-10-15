package io.seqera.tower.cli.teams;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.responses.teams.TeamCreated;
import io.seqera.tower.cli.responses.teams.TeamDeleted;
import io.seqera.tower.cli.responses.teams.TeamsList;
import io.seqera.tower.model.TeamDbDto;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Arrays;
import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TeamsCmdTest extends BaseCmdTest {

    @Test
    void testList(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "list", "-o", "organization1");

        assertEquals("", out.stdErr);
        assertEquals(chop(new TeamsList("organization1", Arrays.asList(
                parseJson(" {\n" +
                        "      \"teamId\": 69076469523589,\n" +
                        "      \"name\": \"team-test-1\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson(" {\n" +
                        "      \"teamId\": 255717345477198,\n" +
                        "      \"name\": \"team-test-2\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson("{\n" +
                        "      \"teamId\": 267477500890054,\n" +
                        "      \"name\": \"team1\",\n" +
                        "      \"description\": \"Team 1\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListEmpty(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"teams\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "list", "-o", "organization1");

        assertEquals("", out.stdErr);
        assertEquals(chop(new TeamsList("organization1", List.of()).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreate(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_create")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "create", "-o", "organization1", "-n", "team-test");

        assertEquals("", out.stdErr);
        assertEquals(new TeamCreated("organization1", "team-test").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithOrganizationNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "create", "-o", "organization-not-found", "-n", "team-test");

        assertEquals(errorMessage(out.app, new OrganizationNotFoundException("organization-not-found")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testDeleteTeam(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/teams/69076469523589"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "teams", "delete", "-o", "organization1", "-i", "69076469523589");

        assertEquals("", out.stdErr);
        assertEquals(new TeamDeleted("organization1", "69076469523589").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
