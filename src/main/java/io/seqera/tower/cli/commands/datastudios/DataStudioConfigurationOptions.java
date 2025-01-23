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

package io.seqera.tower.cli.commands.datastudios;

import java.util.List;

import picocli.CommandLine;

public class DataStudioConfigurationOptions {

    @CommandLine.Option(names = {"-g", "--gpu"}, description = "Optional configuration override for 'gpu' setting (Integer representing number of cores)")
    public Integer gpu;

    @CommandLine.Option(names = {"-c", "--cpu"}, description = "Optional configuration override for 'cpu' setting (Integer representing number of cores)")
    public Integer cpu;

    @CommandLine.Option(names = {"-m", "--memory"}, description = "Optional configuration override for 'memory' setting (Integer representing memory in MBs)")
    public Integer memory;

    @CommandLine.Option(names = {"--mount-data"}, description = "Optional configuration override for 'mountData' setting (comma separate list of datalinkIds)", split = ",")
    public List<String> mountData;

    @CommandLine.Option(names = {"--conda-env"}, description = "Optional configuration override for 'condaEnvironment' setting (YAML conda packages configurations)")
    public String condaEnvironment;

}
