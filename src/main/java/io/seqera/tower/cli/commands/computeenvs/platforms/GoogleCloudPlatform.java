/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.GoogleCloudConfig;
import io.seqera.tower.model.SchedConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;

public class GoogleCloudPlatform extends AbstractPlatform<GoogleCloudConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be a Google Cloud Storage bucket path (e.g., gs://your-bucket/work). Credentials must have read-write access.", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "Google Cloud region where compute instances will be launched (e.g., us-central1, europe-west1).", required = true)
    public String region;

    @Option(names = {"-z", "--zone"}, description = "Google Cloud zone within the region (e.g., us-central1-a). If omitted, defaults to the first zone alphabetically in the region.", required = true)
    public String zone;

    @ArgGroup(heading = "%nScheduler options:%n", validate = false)
    public SchedOptions sched;

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
                .schedEnabled(sched != null && Boolean.TRUE.equals(sched.schedEnabled))

                // Main
                .region(region)
                .zone(zone);

        if (sched != null) {
            SchedConfig schedConfig = new SchedConfig();
            if (sched.provisioningModel != null) {
                schedConfig.provisioningModel(sched.provisioningModel);
            }
            if (sched.machineTypes != null) {
                schedConfig.machineTypes(sched.machineTypes);
            }
            config.schedConfig(schedConfig);
        }

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

    public static class SchedOptions {
        @Option(names = {"--sched-enabled"}, description = "Enable the Seqera scheduler for this compute environment. Defaults to false if not specified.")
        public Boolean schedEnabled;

        @Option(names = {"--provisioning-model"}, description = "Instance provisioning model used by the Seqera scheduler. Valid values: SPOT, SPOT_FIRST, ONDEMAND.")
        public SchedConfig.ProvisioningModelEnum provisioningModel;

        @Option(names = {"--sched-machine-types"}, description = "Compute Engine machine types for compute nodes managed by the Seqera scheduler. Comma-separated list (e.g., n2-standard-4,c2-standard-8). Leave empty to let the scheduler select the most cost-effective types.", split = ",")
        public List<String> machineTypes;
    }

    public static class AdvancedOptions {
        @Option(names = {"--arm64"}, description = "Enable ARM64 (Axion) architecture instances to run compute jobs. Provides efficient compute for compatible workloads.")
        public Boolean arm64Enabled;

        @Option(names = {"--boot-disk-size"}, description = "Boot disk size in GB for Compute Engine instances. Uses pd-standard disk type. If absent, Platform defaults to 50 GB.")
        public Integer bootDiskSizeGb;

        @Option(names = {"--gpu"}, description = "Enable GPU-enabled instances for compute jobs. When enabled, Deep Learning VM base images with CUDA are automatically selected.")
        public Boolean gpuEnabled;

        @Option(names = {"--image-id"}, description = "Image ID defining the operating system and pre-installed software for Compute Engine instances. Supports Ubuntu LTS Google public images. For GPU instances, Deep Learning VM base images with CUDA are automatically selected.")
        public String imageId;

        @Option(names = {"--instance-type"}, description = "Compute Engine machine type (e.g., n1-standard-1, n2-standard-2). If omitted, a default machine type is used.")
        public String instanceType;
    }
}
