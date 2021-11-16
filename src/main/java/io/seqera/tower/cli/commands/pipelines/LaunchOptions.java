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

import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;

public class LaunchOptions {

    @Option(names = {"-c", "--compute-env"}, description = "Compute environment name [default: primary compute environment].")
    public String computeEnv;

    @Option(names = {"--work-dir"}, description = "Path where the pipeline scratch data is stored.")
    public String workDir;

    @Option(names = {"-p", "--profiles"}, split = ",", description = "Comma-separated list of one or more configuration profile names you want to use for this pipeline execution.")
    public List<String> profiles;

    @Option(names = {"--params"}, description = "Pipeline parameters in either JSON or YML format.")
    public Path params;

    @Option(names = {"--revision"}, description = "A valid repository commit Id, tag or branch name.")
    public String revision;

    @Option(names = {"--config"}, description = "Additional Nextflow config settings file.")
    public Path config;

    @Option(names = {"--pre-run"}, description = "Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched.")
    public Path preRunScript;

    @Option(names = {"--post-run"}, description = "Bash script that is executed in the same environment where Nextflow runs immediately after the pipeline completion.")
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
}
