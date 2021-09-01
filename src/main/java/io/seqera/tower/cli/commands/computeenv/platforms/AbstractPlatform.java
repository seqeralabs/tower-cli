package io.seqera.tower.cli.commands.computeenv.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractPlatform<T extends ComputeConfig> implements Platform {

    private PlatformEnum type;

    @Option(names = {"-w", "--work-dir"}, description = "Work directory", required = true)
    public String workDir;

    @Option(names = {"--pre-run-script"}, description = "Pre-run script")
    public Path preRunScript;

    @Option(names = {"--post-run-script"}, description = "Post-run script")
    public Path postRunScript;

    public AbstractPlatform(PlatformEnum type) {
        this.type = type;
    }

    protected String preRunScriptString() throws IOException {
        if (preRunScript == null) {
            return null;
        }
        return FilesHelper.readString(preRunScript);
    }

    protected String postRunScriptString() throws IOException {
        if (postRunScript == null) {
            return null;
        }
        return FilesHelper.readString(postRunScript);
    }

    public PlatformEnum type() {
        return type;
    }

    public abstract T computeConfig() throws ApiException, IOException;



}
