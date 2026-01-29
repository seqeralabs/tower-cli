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

    @CommandLine.Option(names = {"--config"}, description = "Display Nextflow configuration used for this workflow execution.")
    public boolean config;

    @CommandLine.Option(names = {"--params"}, description = "Display pipeline parameters provided at launch time in JSON or YAML format.")
    public boolean params;

    @CommandLine.Option(names = {"--command"}, description = "Display the Nextflow run command used to execute this workflow.")
    public boolean command = false;

    @CommandLine.Option(names = {"--status"}, description = "Display current workflow execution status.")
    public boolean status = false;

    @CommandLine.Option(names = {"--processes"}, description = "Display per-process execution progress showing pending, running, succeeded, failed, and cached task counts.")
    public boolean processes = false;

    @CommandLine.Option(names = {"--stats"}, description = "Display workflow execution statistics including compute time, task counts, success/failure percentages, and cached task efficiency.")
    public boolean stats = false;

    @CommandLine.Option(names = {"--load"}, description = "Display real-time resource usage metrics.")
    public boolean load = false;

    @CommandLine.Option(names = {"--utilization"}, description = "Display resource efficiency metrics showing CPU and memory utilization percentages across workflow execution.")
    public boolean utilization = false;

    @CommandLine.Option(names = {"--metrics-memory"}, description = "Display memory usage statistics per process including mean, min, max, and quartile distributions (RSS, virtual memory).")
    public boolean metricsMemory = false;

    @CommandLine.Option(names = {"--metrics-cpu"}, description = "Display CPU usage statistics per process including mean, min, max, and quartile distributions (CPU time, CPU percentage).")
    public boolean metricsCpu = false;

    @CommandLine.Option(names = {"--metrics-time"}, description = "Display task execution time statistics per process including mean, min, max, and quartile distributions (duration, realtime).")
    public boolean metricsTime = false;

    @CommandLine.Option(names = {"--metrics-io"}, description = "Display I/O statistics per process including mean, min, max, and quartile distributions (read bytes, write bytes, syscalls).")
    public boolean metricsIo = false;
}
