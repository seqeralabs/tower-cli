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

package io.seqera.tower.cli.commands.studios;

import picocli.CommandLine;

public class StudioConfigurationOptions {

    @CommandLine.Option(names = {"--gpu"}, description = "Optional configuration override for 'gpu' setting (integer representing number of cores).")
    public Integer gpu;

    @CommandLine.Option(names = {"--cpu"}, description = "Optional configuration override for 'cpu' setting (integer representing number of cores).")
    public Integer cpu;

    @CommandLine.Option(names = {"--memory"}, description = "Optional configuration override for 'memory' setting (integer representing memory in MBs).")
    public Integer memory;

    @CommandLine.Option(names = {"--lifespan"}, description = "Optional configuration override for 'lifespan' setting (integer representing hours). Defaults to workspace lifespan setting.")
    public Integer lifespan;

    @CommandLine.Mixin
    public DataLinkRefOptions dataLinkRefOptions;

}
