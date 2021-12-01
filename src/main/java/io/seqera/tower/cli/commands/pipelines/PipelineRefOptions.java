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

package io.seqera.tower.cli.commands.pipelines;

import picocli.CommandLine;

public class PipelineRefOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public PipelineRef pipeline;

    public static class PipelineRef {

        @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline unique id.")
        public Long pipelineId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name.")
        public String pipelineName;
    }
}
