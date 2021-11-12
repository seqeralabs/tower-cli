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

    @CommandLine.Option(names = {"--config"}, description = "Display pipeline's run configuration")
    public boolean config;

    @CommandLine.Option(names = {"--params"}, description = "Display pipeline's run parameters")
    public boolean params;

    @CommandLine.Option(names = {"--command"}, description = "Display pipeline's run command")
    public boolean command = false;

    @CommandLine.Option(names = {"--status"}, description = "Display pipeline's run status")
    public boolean status = false;

    @CommandLine.Option(names = {"--processes"}, description = "Display pipeline's run processed")
    public boolean processes = false;

    @CommandLine.Option(names = {"--stats"}, description = "Display pipeline's run stats")
    public boolean stats = false;

    @CommandLine.Option(names = {"--load"}, description = "Display pipeline's run load")
    public boolean load = false;

    @CommandLine.Option(names = {"--utilization"}, description = "Display pipeline's run utilization")
    public boolean utilization = false;

    @CommandLine.Option(names = {"--metrics-memory"}, description = "Display pipeline's run processes memory metrics")
    public boolean metricsMemory = false;

    @CommandLine.Option(names = {"--metrics-cpu"}, description = "Display pipeline's run processes CPU metrics")
    public boolean metricsCpu = false;

    @CommandLine.Option(names = {"--metrics-time"}, description = "Display pipeline's run processes job time metrics")
    public boolean metricsTime = false;

    @CommandLine.Option(names = {"--metrics-io"}, description = "Display pipeline's run processes I/O metrics")
    public boolean metricsIo = false;
}
