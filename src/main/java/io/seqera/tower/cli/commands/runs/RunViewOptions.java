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

package io.seqera.tower.cli.commands.runs;

import picocli.CommandLine;

public class RunViewOptions {

    @CommandLine.Option(names = {"--config"}, description = "Display pipeline run configuration")
    public boolean config;

    @CommandLine.Option(names = {"--params"}, description = "Display pipeline run parameters")
    public boolean params;

    @CommandLine.Option(names = {"--command"}, description = "Display pipeline run command")
    public boolean command = false;

    @CommandLine.Option(names = {"--status"}, description = "Display pipeline run status")
    public boolean status = false;

    @CommandLine.Option(names = {"--processes"}, description = "Display pipeline run processes")
    public boolean processes = false;

    @CommandLine.Option(names = {"--stats"}, description = "Display pipeline run stats")
    public boolean stats = false;

    @CommandLine.Option(names = {"--load"}, description = "Display pipeline run load")
    public boolean load = false;

    @CommandLine.Option(names = {"--utilization"}, description = "Display pipeline run utilization")
    public boolean utilization = false;

    @CommandLine.Option(names = {"--metrics-memory"}, description = "Display pipeline run memory metrics")
    public boolean metricsMemory = false;

    @CommandLine.Option(names = {"--metrics-cpu"}, description = "Display pipeline run CPU metrics")
    public boolean metricsCpu = false;

    @CommandLine.Option(names = {"--metrics-time"}, description = "Display pipeline run job time metrics")
    public boolean metricsTime = false;

    @CommandLine.Option(names = {"--metrics-io"}, description = "Display pipeline run I/O metrics")
    public boolean metricsIo = false;
}
