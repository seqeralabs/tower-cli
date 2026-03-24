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

package io.seqera.tower.cli.runs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.runs.metrics.enums.MetricColumn;
import io.seqera.tower.cli.commands.runs.metrics.enums.MetricPreviewFormat;
import io.seqera.tower.cli.responses.runs.RunViewMetrics;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
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

class MetricsCmdTest extends BaseCmdTest {

    @Test
    void testRunMetricsExpanded(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4/metrics"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/runs_metrics")).withContentType(MediaType.APPLICATION_JSON)
        );

        List<MetricColumn> cols = new ArrayList<>();
        cols.add(MetricColumn.min);
        cols.add(MetricColumn.q1);
        cols.add(MetricColumn.q2);
        cols.add(MetricColumn.q3);
        cols.add(MetricColumn.max);
        cols.add(MetricColumn.mean);

        List<Map<String, Object>> metricsMem = parseJson(new String(loadResource("runs/mem")), List.class);
        List<Map<String, Object>> metricsCpu = parseJson(new String(loadResource("runs/cpu")), List.class);
        List<Map<String, Object>> metricsTime = parseJson(new String(loadResource("runs/time")), List.class);
        List<Map<String, Object>> metricsIo = parseJson(new String(loadResource("runs/io")), List.class);

        ExecOut out = exec(mock,"runs", "view", "-i", "5dAZoXrcmZXRO4", "metrics", "-v", "expanded");
        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new RunViewMetrics(cols, metricsMem, metricsCpu, metricsTime, metricsIo, MetricPreviewFormat.expanded).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testRunMetricsCondensed(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4/metrics"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/runs_metrics")).withContentType(MediaType.APPLICATION_JSON)
        );

        List<MetricColumn> cols = new ArrayList<>();
        cols.add(MetricColumn.min);
        cols.add(MetricColumn.q1);
        cols.add(MetricColumn.q2);
        cols.add(MetricColumn.q3);
        cols.add(MetricColumn.max);
        cols.add(MetricColumn.mean);

        List<Map<String, Object>> metricsMem = parseJson(new String(loadResource("runs/mem")), List.class);
        List<Map<String, Object>> metricsCpu = parseJson(new String(loadResource("runs/cpu")), List.class);
        List<Map<String, Object>> metricsTime = parseJson(new String(loadResource("runs/time")), List.class);
        List<Map<String, Object>> metricsIo = parseJson(new String(loadResource("runs/io")), List.class);

        ExecOut out = exec(mock,"runs", "view", "-i", "5dAZoXrcmZXRO4", "metrics", "-v", "condensed");
        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new RunViewMetrics(cols, metricsMem, metricsCpu, metricsTime, metricsIo, MetricPreviewFormat.condensed).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
