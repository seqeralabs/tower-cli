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

    @Option(names = {"-c", "--compute-env"}, description = "Compute environment identifier where the pipeline will run. Defaults to workspace primary compute environment if omitted. Provide the name or identifier.")
    public String computeEnv;

    @Option(names = {"--work-dir"}, description = "Work directory path where workflow intermediate files are stored. Defaults to compute environment work directory if omitted.")
    public String workDir;

    @Option(names = {"-p", "--profile"}, split = ",", description = "Array of Nextflow configuration profile names to apply.")
    public List<String> profile;

    @Option(names = {"--params-file"}, description = "Pipeline parameters in JSON or YAML format. Provide the path to a file containing the content.")
    public Path paramsFile;

    @Option(names = {"--revision"}, description = "Git revision, branch, or tag to use. Use --commit-id to pin to a specific commit within the revision.")
    public String revision;

    @Option(names = {"--commit-id"}, description = "Specific Git commit hash to pin the pipeline execution to.")
    public String commitId;

    @Option(names = {"--config"}, description = "Nextflow configuration as text (overrides config files). Provide the path to a file containing the content.")
    public Path config;

    @Option(names = {"--pre-run"}, description = "Add a script that executes in the nf-launch script prior to invoking Nextflow processes. See: https://docs.seqera.io/platform-cloud/launch/advanced#pre-and-post-run-scripts. Provide the path to a file containing the content.")
    public Path preRunScript;

    @Option(names = {"--post-run"}, description = "Add a script that executes after all Nextflow processes have completed. See: https://docs.seqera.io/platform-cloud/launch/advanced#pre-and-post-run-scripts. Provide the path to a file containing the content.")
    public Path postRunScript;

    @Option(names = {"--pull-latest"}, description = "Pull the latest version of the pipeline from the repository.")
    public Boolean pullLatest;

    @Option(names = {"--stub-run"}, description = "Execute a stub run for testing (processes return dummy results).")
    public Boolean stubRun;

    @Option(names = {"--main-script"}, description = "Alternative main script filename. Default: `main.nf`.")
    public String mainScript;

    @Option(names = {"--entry-name"}, description = "Workflow entry point name when using Nextflow DSL2.")
    public String entryName;

    @Option(names = {"--schema-name"}, description = "Name of the pipeline schema to use.")
    public String schemaName;

    @Option(names = {"--user-secrets"}, split = ",", description = "Array of user secrets to make available to the pipeline.")
    public List<String> userSecrets;

    @Option(names = {"--workspace-secrets"}, split = ",", description = "Array of workspace secrets to make available to the pipeline.")
    public List<String> workspaceSecrets;
}
