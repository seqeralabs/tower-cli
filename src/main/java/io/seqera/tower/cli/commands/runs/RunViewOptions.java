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

package io.seqera.tower.cli.commands.runs;

import picocli.CommandLine;

public class RunViewOptions {

    @CommandLine.Option(names = {"--config"}, description = "Display pipeline run configuration.")
    public boolean config;

    @CommandLine.Option(names = {"--params"}, description = "Display pipeline run parameters.")
    public boolean params;

    @CommandLine.Option(names = {"--command"}, description = "Display pipeline run command.")
    public boolean command = false;

    @CommandLine.Option(names = {"--status"}, description = "Display pipeline run status.")
    public boolean status = false;

    @CommandLine.Option(names = {"--processes"}, description = "Display pipeline run processes.")
    public boolean processes = false;

    @CommandLine.Option(names = {"--stats"}, description = "Display pipeline run stats.")
    public boolean stats = false;

    @CommandLine.Option(names = {"--load"}, description = "Display pipeline run load.")
    public boolean load = false;

    @CommandLine.Option(names = {"--utilization"}, description = "Display pipeline run utilization.")
    public boolean utilization = false;

    @CommandLine.Option(names = {"--metrics-memory"}, description = "Display pipeline run memory metrics.")
    public boolean metricsMemory = false;

    @CommandLine.Option(names = {"--metrics-cpu"}, description = "Display pipeline run CPU metrics.")
    public boolean metricsCpu = false;

    @CommandLine.Option(names = {"--metrics-time"}, description = "Display pipeline run job time metrics.")
    public boolean metricsTime = false;

    @CommandLine.Option(names = {"--metrics-io"}, description = "Display pipeline run I/O metrics.")
    public boolean metricsIo = false;
}
