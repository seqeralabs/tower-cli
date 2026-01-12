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
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.GoogleCloudConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GoogleCloudPlatform extends AbstractPlatform<GoogleCloudConfig> {

    @Option(names = {"--work-dir"}, description = "Work directory.", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "Google Cloud region.", required = true)
    public String region;

    @Option(names = {"-z", "--zone"}, description = "Google Cloud zone within the region.", required = true)
    public String zone;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public GoogleCloudPlatform() {
        super(PlatformEnum.GOOGLE_CLOUD);
    }

    @Override
    public GoogleCloudConfig computeConfig() throws ApiException, IOException {
        GoogleCloudConfig config = new GoogleCloudConfig();

        config
                .waveEnabled(true)
                .fusion2Enabled(true)

                // Main
                .region(region)
                .zone(zone);

        // Advanced
        if (adv != null) {
            config
                    .arm64Enabled(adv.arm64Enabled)
                    .gpuEnabled(adv.gpuEnabled)
                    .imageId(adv.imageId)
                    .instanceType(adv.instanceType)
                    .bootDiskSizeGb(adv.bootDiskSizeGb);
        }

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    public static class AdvancedOptions {
        @Option(names = {"--arm64"}, description = "Use ARM64 (Axion) based instances.")
        public Boolean arm64Enabled;

        @Option(names = {"--boot-disk-size"}, description = "Boot disk size in GB.")
        public Integer bootDiskSizeGb;

        @Option(names = {"--gpu"}, description = "Enable GPU-enabled instances.")
        public Boolean gpuEnabled;

        @Option(names = {"--image-id"}, description = "Image ID for the Compute Engine instance.")
        public String imageId;

        @Option(names = {"--instance-type"}, description = "Compute Engine machine type.")
        public String instanceType;
    }
}
