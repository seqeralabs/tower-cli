package io.seqera.tower.cli.commands.computeenv.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractPlatform {

    private PlatformEnum type;

    @Option(names = {"-w", "--work-dir"}, description = "Work directory")
    public String workDir;

    @Option(names = {"--pre-run-script"}, description = "Pre-run script")
    public Path preRunScript;

    @Option(names = {"--post-run-script"}, description = "Post-run script")
    private Path postRunScript;

    public AbstractPlatform(PlatformEnum type) {
        this.type = type;
    }

    public PlatformEnum type() {
        return type;
    }

    public ComputeConfig computeConfig() throws IOException, ApiException {
        ComputeConfig res = new ComputeConfig();
        res.workDir(workDir);
        if (preRunScript != null) {
            res.preRunScript(Files.readString(preRunScript));
        }
        if (postRunScript != null) {
            res.postRunScript(Files.readString(postRunScript));
        }
        return res;
    }
}
