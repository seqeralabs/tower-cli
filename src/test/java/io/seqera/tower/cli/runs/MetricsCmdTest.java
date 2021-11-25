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
import io.seqera.tower.cli.commands.runs.metrics.enums.MetricColumn;
import io.seqera.tower.cli.responses.RunViewMetrics;
import org.junit.jupiter.api.Test;
import org.apache.commons.lang3.StringUtils;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MetricsCmdTest extends BaseCmdTest {

    @Test
    void testRunMetrics(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4/metrics"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/runs_metrics")).withContentType(MediaType.APPLICATION_JSON)
        );

        List<MetricColumn> cols = new ArrayList<>();
        cols.add(MetricColumn.q1);
        cols.add(MetricColumn.q2);
        cols.add(MetricColumn.q3);
        cols.add(MetricColumn.min);
        cols.add(MetricColumn.max);
        cols.add(MetricColumn.mean);

        List<Map<String, Object>> metricsMem = parseJson(new String(loadResource("runs/mem")), List.class);
        List<Map<String, Object>> metricsCpu = parseJson(new String(loadResource("runs/cpu")), List.class);
        List<Map<String, Object>> metricsTime = parseJson(new String(loadResource("runs/time")), List.class);
        List<Map<String, Object>> metricsIo = parseJson(new String(loadResource("runs/io")), List.class);

        ExecOut out = exec(mock,"runs", "view", "-i", "5dAZoXrcmZXRO4", "metrics");
        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new RunViewMetrics(cols, metricsMem, metricsCpu, metricsTime, metricsIo, true).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
