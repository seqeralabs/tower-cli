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
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.GoogleCloudConfig;
import io.seqera.tower.model.SchedConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class GoogleCloudPlatform extends AbstractPlatform<GoogleCloudConfig> {

    private static final Pattern NETWORK_TAG_PATTERN = Pattern.compile("^[a-z][-a-z0-9]*[a-z0-9]$");
    private static final int MAX_NETWORK_TAGS = 64;
    private static final int MAX_TAG_LENGTH = 63;

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

    @Option(names = {"--fusion-metrics-collection"}, negatable = true, description = "Send Fusion metrics to Seqera for this compute environment. Fusion always generates the metrics; this only controls whether they are collected and sent to Seqera. Only valid when Fusion is enabled. If unset, Platform applies its default.")
    public Boolean fusionMetricsCollection;

    @Override
    public Boolean fusionMetricsCollectionEnabled() {
        return fusionMetricsCollection;
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
            if (adv.networkTags != null && !adv.networkTags.isEmpty()) {
                validateNetworkTags(adv.networkTags, adv.network);
            }

            config
                    .arm64Enabled(adv.arm64Enabled)
                    .gpuEnabled(adv.gpuEnabled)
                    .imageId(adv.imageId)
                    .instanceType(adv.instanceType)
                    .bootDiskSizeGb(adv.bootDiskSizeGb)
                    .network(adv.network)
                    .subnetworks(adv.subnetworks)
                    .networkTags(adv.networkTags)
                    .usePrivateAddress(adv.usePrivateAddress);
        }

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    private static void validateNetworkTags(List<String> tags, String network) {
        if (network == null || network.isEmpty()) {
            throw new TowerRuntimeException("Network tags require VPC configuration: set the '--network' option to use network tags.");
        }

        if (tags.size() > MAX_NETWORK_TAGS) {
            throw new TowerRuntimeException(String.format("Too many network tags: maximum is %d, provided %d.", MAX_NETWORK_TAGS, tags.size()));
        }

        for (String tag : tags) {
            if (tag == null || tag.isEmpty() || tag.length() > MAX_TAG_LENGTH) {
                throw new TowerRuntimeException(String.format("Invalid network tag '%s': must be 1-63 characters.", tag));
            }
            if (tag.length() == 1) {
                if (!tag.matches("^[a-z]$")) {
                    throw new TowerRuntimeException(String.format("Invalid network tag '%s': single-character tags must be a lowercase letter.", tag));
                }
            } else {
                if (!NETWORK_TAG_PATTERN.matcher(tag).matches()) {
                    throw new TowerRuntimeException(String.format("Invalid network tag '%s': must start with a lowercase letter, end with a letter or number, and contain only lowercase letters, numbers, and hyphens.", tag));
                }
            }
        }
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

        @Option(names = {"--network"}, description = "Google Cloud VPC network name or URI. Required when using subnetworks or network tags. When omitted, the project's 'default' network is used.")
        public String network;

        @Option(names = {"--subnetworks"}, split = ",", paramLabel = "<subnetwork>", description = "Google Cloud VPC subnetworks for instance placement. Comma-separated list of names or URIs in the same region as the compute environment; the first is used for basic placement while Intelligent Compute may use all of them. Requires --network.")
        public List<String> subnetworks;

        @Option(names = {"--network-tags"}, split = ",", paramLabel = "<tag>", description = "Comma-separated list of network tags applied to VMs for firewall rule targeting. Tags must be lowercase, use only letters, numbers, and hyphens (1-63 chars). Requires --network.")
        public List<String> networkTags;

        @Option(names = {"--use-private-address"}, description = "Do not attach a public IP address to VM instances. When enabled, only Google internal services are accessible. Requires Cloud NAT for external access.")
        public Boolean usePrivateAddress;
    }
}
