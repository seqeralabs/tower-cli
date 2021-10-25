package io.seqera.tower.cli.teams.members;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.responses.teams.TeamsList;
import io.seqera.tower.cli.responses.teams.members.TeamMembersList;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.TeamDbDto;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TeamMembersCmdTest extends BaseCmdTest {

    @Test
    void testList(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(2)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/{orgId}/teams/{teamId}/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "members", "-i", "267477500890054");

        assertEquals("", out.stdErr);
        assertEquals(chop(new TeamMembersList(267477500890054L, Arrays.asList(
                parseJson("  {\n" +
                        "      \"memberId\": 80726606082762,\n" +
                        "      \"userName\": \"julio2\",\n" +
                        "      \"email\": \"julio2@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": null,\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class),
                parseJson(" {\n" +
                        "      \"memberId\": 142050384398323,\n" +
                        "      \"userName\": \"julio789\",\n" +
                        "      \"email\": \"julio789@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": null,\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class),
                parseJson(" {\n" +
                        "      \"teamId\": 255717345477198,\n" +
                        "      \"name\": \"team-test-2\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", MemberDbDto.class),
                parseJson(" {\n" +
                        "      \"memberId\": 199966343791197,\n" +
                        "      \"userName\": \"julio7899\",\n" +
                        "      \"email\": \"julio7899@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": null,\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAdd(MockServerClient mock) {

    }

    @Test
    void testDelete(MockServerClient mock) {

    }
}
