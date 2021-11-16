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

package io.seqera.tower.cli.runs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.runs.tasks.enums.TaskColumn;
import org.junit.jupiter.api.Test;
import io.seqera.tower.cli.responses.RunTasksView;
import org.apache.commons.lang3.StringUtils;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TasksCmdTest extends BaseCmdTest {

    @Test
    void listRunTasksTests(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/2zGxKoqlnVmGL/tasks"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/tasks_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "view", "-i", "2zGxKoqlnVmGL", "tasks");

        List<String> cols = Stream.of(TaskColumn.values())
                .filter(it -> it.isFixed())
                .map(TaskColumn::getDescription)
                .collect(Collectors.toList());

        List<List<String>> tasks = new ArrayList<>();
        List<String> item1 = new ArrayList<>();
        item1.add("1");
        item1.add("NFCORE_RNASEQ:RNASEQ:PREPARE_GENOME:GUNZIP_ADDITIONAL_FASTA");
        item1.add("gfp.fa.gz");
        item1.add("COMPLETED");


        List<String> item2 = new ArrayList<>();
        item2.add("2");
        item2.add("NFCORE_RNASEQ:RNASEQ:PREPARE_GENOME:UNTAR_STAR_INDEX");
        item2.add("star.tar.gz");
        item2.add("COMPLETED");


        tasks.add(item1);
        tasks.add(item2);

        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new RunTasksView("2zGxKoqlnVmGL", cols, tasks).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
