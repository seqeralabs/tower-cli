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
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.ConfigEnvVariable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractPlatform<T extends ComputeConfig> implements Platform {

    @Option(names = {"--work-dir"}, description = "Work directory.", required = true)
    public String workDir;

    @ArgGroup(heading = "%nStaging options:%n", validate = false)
    public StagingOptions staging;

    @ArgGroup(heading = "%nEnvironment variables:%n", validate = false)
    public Environment environment;

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

    protected List<ConfigEnvVariable> environmentVariables() {
        if (environment == null || environment.variables == null || environment.variables.size() == 0) {
            return null;
        }

        List<ConfigEnvVariable> vars = new ArrayList<>(environment.variables.size());
        environment.variables.forEach((name, value) -> {
            boolean head = true;
            boolean compute = false;
            String varName = name;
            if (name.startsWith("compute:")) {
                varName = name.substring(8);
                head = false;
                compute = true;
            } else if (name.startsWith("head:")) {
                varName = name.substring(5);
                compute = false;
            } else if (name.startsWith("both:")) {
                varName = name.substring(5);
                compute = true;
            }
            vars.add(new ConfigEnvVariable().name(varName).value(value).head(head).compute(compute));
        });

        return vars;
    }

    public PlatformEnum type() {
        return type;
    }

    public T computeConfig(Long workspaceId, DefaultApi api) throws ApiException, IOException {
        return computeConfig();
    }

    public T computeConfig() throws ApiException, IOException {
        throw new UnsupportedOperationException();
    }

    public static class StagingOptions {
        @Option(names = {"--pre-run"}, description = "Pre-run script.")
        public Path preRunScript;

        @Option(names = {"--post-run"}, description = "Post-run script.")
        public Path postRunScript;
    }

    public static class Environment {
        @Option(names = {"-e", "--env"}, description = "Add environment variables. By default are only added to the Nextflow " +
                "head job process, if you want to add them to the process task prefix the name with 'compute:' or 'both:' if you want to " +
                "make it available to both locations.")
        Map<String, String> variables;
    }


}
