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

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.CredentialsApi;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.ConfigEnvVariable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
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

    public T computeConfig(Long workspaceId, CredentialsApi api) throws ApiException, IOException {
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
