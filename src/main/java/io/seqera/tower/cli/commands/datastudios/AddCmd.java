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

import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(
        name = "add",
        description = "Add new data studio."
)
public class AddCmd extends AbstractStudiosCmd{

    @CommandLine.Option(names = {"-n", "--name"}, description = "Data Studio name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Data studio description.")
    public String description;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    // completion candidates?
    // preprocessor ?
    @CommandLine.Option(names = {"-t", "--template"}, description = "Data studio template container image. Available templates can be listed with 'studios templates' command", required = true)
    public String template;

    // add template endpoint


    @CommandLine.Option(names = {"-c", "--compute-env"}, description = "Compute environment name.", required = true)
    public String computeEnv;

    @CommandLine.Option(names = {"--gpu"}, description = "The number of GPUs allocated to the data studio, defaults to 0", defaultValue = "0")
    public String gpu;

    @CommandLine.Option(names = {"--cpu"}, description = "The number of CPUs allocated to the data studio, defaults to 2", defaultValue = "2")
    public String cpu;

    @CommandLine.Option(names = {"--memory"}, description = "The maximum memory allocated to the data studio, defaults to 8192", defaultValue = "8192")
    public String memory;

    //mounted data ????

    // start as new  - how to model it???
    @CommandLine.Option(names = {"--conda-env-yml", "--conda-env-yaml"}, description = "Path to a YAML env file with Conda packages to be installed in the Data Studio environment.")
    public Path condaEnv;

    @CommandLine.Option(names = {"-a", "--autoStart"}, description = "Create Data Studio and start it immediately, defaults to true", defaultValue = "true")
    public Boolean autoStart;
}
