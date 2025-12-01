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

import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;

public class LaunchOptions {

    @Option(names = {"-c", "--compute-env"}, description = "Compute environment name.")
    public String computeEnv;

    @Option(names = {"--work-dir"}, description = "Path where the pipeline scratch data is stored.")
    public String workDir;

    @Option(names = {"-p", "--profile"}, split = ",", description = "Comma-separated list of one or more configuration profile names you want to use for this pipeline execution.")
    public List<String> profile;

    @Option(names = {"--params-file"}, description = "Pipeline parameters in either JSON or YML format. Use '-' to read from stdin.")
    public Path paramsFile;

    @Option(names = {"--revision"}, description = "A valid repository commit Id, tag or branch name.")
    public String revision;

    @Option(names = {"--config"}, description = "Path to a Nextflow config file. Values defined here override the same values in the pipeline repository config file, and all configuration values specified in Platform pipeline or compute environment Nextflow config fields are ignored. Use '-' to read from stdin.")
    public Path config;

    @Option(names = {"--pre-run"}, description = "Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched. Use '-' to read from stdin.")
    public Path preRunScript;

    @Option(names = {"--post-run"}, description = "Bash script that is executed in the same environment where Nextflow runs immediately after the pipeline completion. Use '-' to read from stdin.")
    public Path postRunScript;

    @Option(names = {"--pull-latest"}, description = "Enable Nextflow to pull the latest repository version before running the pipeline.")
    public Boolean pullLatest;

    @Option(names = {"--stub-run"}, description = "Execute the workflow replacing process scripts with command stubs.")
    public Boolean stubRun;

    @Option(names = {"--main-script"}, description = "Pipeline main script file if different from `main.nf`.")
    public String mainScript;

    @Option(names = {"--entry-name"}, description = "Main workflow name to be executed when using DLS2 syntax.")
    public String entryName;

    @Option(names = {"--schema-name"}, description = "Schema name.")
    public String schemaName;

    @Option(names = {"--user-secrets"}, split = ",", description = "Pipeline Secrets required by the pipeline execution that belong to the launching user personal context. User's secrets will take precedence over workspace secrets with the same name.")
    public List<String> userSecrets;

    @Option(names = {"--workspace-secrets"}, split = ",", description = "Pipeline Secrets required by the pipeline execution. Those secrets must be defined in the launching workspace.")
    public List<String> workspaceSecrets;
}
