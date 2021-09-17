package io.seqera.tower.cli.commands.runs;

import java.nio.file.Path;
import java.util.List;

import picocli.CommandLine.Option;

public class RunOptions {

    @Option(names = {"-c", "--compute-env"}, description = "Compute environment name")
    public String computeEnv;

    @Option(names = {"-l", "--pipeline"}, description = "Pipeline URL")
    public String pipeline;

    @Option(names = {"-w", "--work-dir"}, description = "Path where the pipeline scratch data is stored")
    public String workDir;

    @Option(names = {"-p", "--profiles"}, split = ",", description = "One or more (separated by comma) configuration profile names you want to use for this pipeline execution")
    public List<String> profiles;

    @Option(names = {"--params"}, description = "Pipeline parameters using either JSON or YML file")
    public Path params;

    @Option(names = {"--revision"}, description = "A valid repository commit Id, tag or branch name")
    public String revision;

    @Option(names = {"--config"}, description = "Additional Nextflow config settings file")
    public Path config;

    @Option(names = {"--pre-run"}, description = "Bash script that's executed in the same environment where Nextflow runs just before the pipeline is launched")
    public Path preRunScript;

    @Option(names = {"--post-run"}, description = "Bash script that's executed in the same environment where Nextflow runs immediately after the pipeline completion")
    public Path postRunScript;

    @Option(names = {"--pull-latest"}, description = "Enabling this option Nextflow pulls the latest version from the Git repository before run the pipeline")
    public Boolean pullLatest;

    @Option(names = {"--stub-run"}, description = "Execute the workflow replacing process scripts with command stubs")
    public Boolean stubRun;

    @Option(names = {"--main-script"}, description = "Pipeline main script file if different from `main.nf`")
    public String mainScript;

    @Option(names = {"--entry-name"}, description = "Main workflow name to be executed when using DLS2 syntax")
    public String entryName;

    @Option(names = {"--schema-name"}, description = "Schema name")
    public String schemaName;

    @Option(names = {"--resume"}, description = "Resume option")
    public Boolean resume;
}
