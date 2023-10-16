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
