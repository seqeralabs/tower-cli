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

package io.seqera.tower.cli.labels;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.labels.DeleteLabelsResponse;
import io.seqera.tower.cli.responses.labels.LabelAdded;
import io.seqera.tower.cli.responses.labels.ListLabelsCmdResponse;
import io.seqera.tower.model.LabelDbDto;
import io.seqera.tower.model.LabelType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.List;

import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class LabelsCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddSimple(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/labels"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(
                        // language=json
                        """
                        {
                            "id": 10,
                            "name": "some-label",
                            "resource": false,
                            "value": null
                            
                        }
                        """).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "labels", "add", "-n", "some-label","-w", "123");
        assertOutput(format, out, new LabelAdded(10l,"some-label",false,null,"123"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddResource(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("POST")
                        .withPath("/labels"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(
                // language=json
                """
                {
                    "id": 10,
                    "name": "res",
                    "resource": true,
                    "value": "val"
                }
                """)
        );

        ExecOut out = exec(format, mock,"labels", "add", "-n", "res","-v","val","-w","4343");
        assertOutput(format,out, new LabelAdded(10L, "res", true,"val","4343"));
    }


    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteLabel(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/labels/1234"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );
        ExecOut out = exec(format, mock, "labels", "delete", "-i", "1234","-w", "5662");
        assertOutput(format, out, new DeleteLabelsResponse(1234L,5662L));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListLabels(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/labels")
                        .withQueryStringParameter("workspaceId","5662512677752")
                        .withQueryStringParameter("max","100")
                        .withQueryStringParameter("offset","0")
                        .withQueryStringParameter("type","all"), exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody( // language=json
                        """
                            {"labels":[
                                {"id":97027588903667,"name":"awesome-label","value":null,"resource":false},
                                {"id":250670995082875,"name":"new-label","value":null,"resource":false},
                                {"id":55286297817389,"name":"newx-label","value":null,"resource":false},
                                {"id":232243090533688,"name":"res-label","value":"aaaa","resource":true}],
                                "totalSize":4}
                        """)
        );
        ExecOut out = exec(format, mock, "labels", "list", "-w","5662512677752");
        assertOutput(format, out, new ListLabelsCmdResponse(
                5662512677752L,
                LabelType.ALL,
                List.of(makeLabelDbDto(97027588903667L,"awesome-label",null, false),
                        makeLabelDbDto(250670995082875L, "new-label", null, false),
                        makeLabelDbDto(55286297817389L, "newx-label",null, false),
                        makeLabelDbDto(232243090533688L, "res-label","aaaa", true))
        ));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListLabelsWithSimpleTypeFilter(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/labels")
                        .withQueryStringParameter("workspaceId","5662512677752")
                        .withQueryStringParameter("max","100")
                        .withQueryStringParameter("offset","0")
                        .withQueryStringParameter("type","simple"), exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody( // language=json
                                """
                                    {"labels":[
                                        {"id":97027588903667,"name":"awesome-label","value": null, "resource":false},
                                        {"id":250670995082875,"name":"new-label","value": null, "resource":false},
                                        {"id":55286297817389,"name":"newx-label","value": null, "resource":false}],
                                        "totalSize":3}
                                """)
        );
        ExecOut out = exec(format, mock, "labels", "list", "-w","5662512677752","-t", "simple");
        assertOutput(format, out, new ListLabelsCmdResponse(
                5662512677752L,
                LabelType.SIMPLE,
                List.of(makeLabelDbDto(97027588903667L,"awesome-label",null, false),
                        makeLabelDbDto(250670995082875L, "new-label", null, false),
                        makeLabelDbDto(55286297817389L, "newx-label",null, false))
        ));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListLabelsWithResourceTypeFilter(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/labels")
                        .withQueryStringParameter("workspaceId","5662512677752")
                        .withQueryStringParameter("max","100")
                        .withQueryStringParameter("offset","0")
                        .withQueryStringParameter("type","resource"), exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody( // language=json
                                """
                                    {"labels":[
                                        {"id":97027588903667,"name":"awesome-label","value": "aaa", "resource":true},
                                        {"id":250670995082875,"name":"new-label","value": "bbb", "resource":true},
                                        {"id":55286297817389,"name":"newx-label","value": "ccc", "resource":true}],
                                        "totalSize":3}
                                """)
        );
        ExecOut out = exec(format, mock, "labels", "list", "-w","5662512677752","-t", "resource");
        assertOutput(format, out, new ListLabelsCmdResponse(
                5662512677752L,
                LabelType.RESOURCE,
                List.of(makeLabelDbDto(97027588903667L,"awesome-label","aaa", true),
                        makeLabelDbDto(250670995082875L, "new-label", "bbb", true),
                        makeLabelDbDto(55286297817389L, "newx-label","ccc", true))
        ));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListLabelsWithTextFilter(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/labels")
                        .withQueryStringParameter("workspaceId","5662512677752")
                        .withQueryStringParameter("max","100")
                        .withQueryStringParameter("offset","0")
                        .withQueryStringParameter("search","find"), exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody( // language=json
                                """
                                    {"labels":[
                                        {"id":97027588903667,"name":"res-find-label","value": "aaa", "resource":true},
                                        {"id":250670995082875,"name":"label-to-find","value": null, "resource":false},
                                        {"id":55286297817389,"name":"find-label","value": "ccc", "resource":true}],
                                        "totalSize":3}
                                """)
        );
        ExecOut out = exec(format, mock, "labels", "list", "-w","5662512677752","-f", "find");
        assertOutput(format, out, new ListLabelsCmdResponse(
                5662512677752L,
                LabelType.ALL,
                List.of(makeLabelDbDto(97027588903667L,"res-find-label","aaa", true),
                        makeLabelDbDto(250670995082875L, "label-to-find", null, false),
                        makeLabelDbDto(55286297817389L, "find-label","ccc", true))
        ));
    }


    private static LabelDbDto makeLabelDbDto(Long id,String name, String value,Boolean resource) {
        var dto = new LabelDbDto();
        dto.setId(id);
        dto.setName(name);
        dto.setResource(resource);
        dto.setValue(value);
        return dto;
    }
}