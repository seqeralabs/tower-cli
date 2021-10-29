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

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractPlatform<T extends ComputeConfig> implements Platform {

    @Option(names = {"-w", "--work-dir"}, description = "Work directory", required = true)
    public String workDir;
    @ArgGroup(heading = "%nStaging options:%n", validate = false)
    public StagingOptions staging;
    private PlatformEnum type;

    public AbstractPlatform(PlatformEnum type) {
        this.type = type;
    }

    protected String preRunScriptString() throws IOException {
        if (staging == null || staging.preRunScript == null) {
            return null;
        }
        return FilesHelper.readString(staging.preRunScript);
    }

    protected String postRunScriptString() throws IOException {
        if (staging == null || staging.postRunScript == null) {
            return null;
        }
        return FilesHelper.readString(staging.postRunScript);
    }

    public PlatformEnum type() {
        return type;
    }

    public abstract T computeConfig() throws ApiException, IOException;

    public static class StagingOptions {
        @Option(names = {"--pre-run"}, description = "Pre-run script")
        public Path preRunScript;

        @Option(names = {"--post-run"}, description = "Post-run script")
        public Path postRunScript;
    }


}
