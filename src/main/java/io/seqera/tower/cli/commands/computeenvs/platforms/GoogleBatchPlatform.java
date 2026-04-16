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
import io.seqera.tower.model.GoogleBatchConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class GoogleBatchPlatform extends AbstractPlatform<GoogleBatchConfig> {

    private static final Pattern NETWORK_TAG_PATTERN = Pattern.compile("^[a-z][-a-z0-9]*[a-z0-9]$");
    private static final Pattern MACHINE_TYPE_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(-[a-z0-9*]+)*$");
    private static final Pattern BOOT_DISK_IMAGE_PATTERN = Pattern.compile("^(projects/[a-z0-9\\-_]+/global/images/(family/)?[a-z0-9\\-_]+|batch-[a-z0-9\\-]+)$");
    private static final int MAX_NETWORK_TAGS = 64;
    private static final int MAX_TAG_LENGTH = 63;

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be a Google Cloud Storage bucket path (e.g., gs://your-bucket/work).", required = true)
    public String workDir;

    @Option(names = {"-l", "--location"}, description = "Google Cloud region where job executions are deployed to Google Batch API (e.g., us-central1, europe-west1).", required = true)
    public String location;

    @Option(names = {"--spot"}, description = "Use Spot virtual machines. Enables cost-effective preemptible instances for compute workloads. Spot VMs may be interrupted when capacity is needed.")
    public Boolean spot;

    @Option(names = {"--fusion-v2"}, description = "Enable Fusion file system. Provides native access to Google Cloud Storage with low-latency I/O. Requires Wave containers.")
    public boolean fusionV2;

    @Option(names = {"--fusion-snapshots"}, description = "Enable Fusion Snapshots (beta). Allows Fusion to restore jobs interrupted by Spot VM reclamation. Requires Fusion v2.")
    public boolean fusionSnapshots;

    @Option(names = {"--wave"}, description = "Enable Wave containers. Allows access to private container repositories and on-demand container provisioning.")
    public boolean wave;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public GoogleBatchPlatform() {
        super(PlatformEnum.GOOGLE_BATCH);
    }

    @Override
    public GoogleBatchConfig computeConfig() throws ApiException, IOException {
        GoogleBatchConfig config = new GoogleBatchConfig();

        if (fusionSnapshots && !fusionV2) {
            throw new TowerRuntimeException("Fusion Snapshots requires Fusion v2 to be enabled (--fusion-v2).");
        }

        config
                .fusion2Enabled(fusionV2)
                .fusionSnapshots(fusionSnapshots)
                .waveEnabled(wave)

                // Main
                .location(location)
                .spot(spot);

        // Advanced
        if (adv != null) {
            if (adv.networkTags != null && !adv.networkTags.isEmpty()) {
                validateNetworkTags(adv.networkTags, adv.network);
            }
            validateMachineTypes(adv);
            validateBootDiskImage(adv.bootDiskImage);

            config
                .network(adv.network)
                .subnetwork(adv.subnetwork)
                .networkTags(adv.networkTags)
                .usePrivateAddress(adv.usePrivateAddress)
                .bootDiskSizeGb(adv.bootDiskSizeGb)
                .bootDiskImage(adv.bootDiskImage)
                .headJobCpus(adv.headJobCpus)
                .headJobMemoryMb(adv.headJobMemoryMb)
                .machineType(adv.headJobMachineType)
                .computeJobsMachineType(adv.computeJobsMachineType)
                .serviceAccount(adv.serviceAccountEmail)
                .headJobInstanceTemplate(adv.headJobInstanceTemplate)
                .computeJobsInstanceTemplate(adv.computeJobInstanceTemplate);
        }

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    private static void validateMachineTypeFormat(String machineType) {
        if (!MACHINE_TYPE_PATTERN.matcher(machineType).matches()) {
            throw new TowerRuntimeException(String.format("Invalid machine type '%s': must contain only lowercase letters, numbers, and hyphens.", machineType));
        }
    }

    private static void validateMachineTypes(AdvancedOptions adv) {
        if (adv.headJobMachineType != null && adv.headJobInstanceTemplate != null) {
            throw new TowerRuntimeException("Head job machine type and head job instance template are mutually exclusive -- specify only one.");
        }
        if (adv.computeJobsMachineType != null && !adv.computeJobsMachineType.isEmpty() && adv.computeJobInstanceTemplate != null) {
            throw new TowerRuntimeException("Compute jobs machine type and compute jobs instance template are mutually exclusive -- specify only one.");
        }
        if (adv.headJobMachineType != null) {
            if (adv.headJobMachineType.contains("*")) {
                throw new TowerRuntimeException("Wildcard machine type families are not supported for the head job -- select a specific machine type instead.");
            }
            validateMachineTypeFormat(adv.headJobMachineType);
        }
        if (adv.computeJobsMachineType != null) {
            for (String mt : adv.computeJobsMachineType) {
                validateMachineTypeFormat(mt);
            }
        }
    }

    private static void validateBootDiskImage(String bootDiskImage) {
        if (bootDiskImage != null && !BOOT_DISK_IMAGE_PATTERN.matcher(bootDiskImage).matches()) {
            throw new TowerRuntimeException("Invalid boot disk image format. Use projects/{PROJECT}/global/images/{IMAGE}, projects/{PROJECT}/global/images/family/{FAMILY}, or a Batch image name (e.g., batch-debian).");
        }
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

    public static class AdvancedOptions {
        @Option(names = {"--network"}, description = "Google Cloud VPC network name or URI. Required when using network tags or subnets.")
        public String network;

        @Option(names = {"--subnetwork"}, description = "Google Cloud VPC subnetwork name or URI. Must be in the same region as the compute environment location.")
        public String subnetwork;

        @Option(names = {"--network-tags"}, split = ",", paramLabel = "<tag>", description = "Comma-separated list of network tags applied to VMs for firewall rule targeting. Tags must be lowercase, use only letters, numbers, and hyphens (1-63 chars). Requires --network.")
        public List<String> networkTags;

        @Option(names = {"--use-private-address"}, description = "Do not attach a public IP address to VM instances. When enabled, only Google internal services are accessible. Requires Cloud NAT for external access.")
        public Boolean usePrivateAddress;

        @Option(names = {"--boot-disk-size"}, description = "Boot disk size in GB. Controls the root volume size for compute instances. If absent, Platform defaults to 50 GB.")
        public Integer bootDiskSizeGb;

        @Option(names = {"--boot-disk-image"}, description = "Custom boot disk image for compute job VMs. Accepts: projects/{PROJECT}/global/images/{IMAGE}, projects/{PROJECT}/global/images/family/{FAMILY}, or a Batch image name (e.g., batch-debian).")
        public String bootDiskImage;

        @Option(names = {"--head-job-cpus"}, description = "Number of CPUs allocated to the Nextflow head job. Controls the compute resources for the main workflow orchestration process.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "Memory allocation for the Nextflow head job in megabytes. Value must be a multiple of 256 MiB and from 0.5 GB to 8 GB per CPU.")
        public Integer headJobMemoryMb;

        @Option(names = {"--service-account-email"}, description = "Google Cloud service account email for pipeline execution. Grants fine-grained IAM permissions to Nextflow jobs.")
        public String serviceAccountEmail;

        @Option(names = {"--head-job-machine-type"}, description = "GCP machine type for the Nextflow head job (e.g., n2-standard-4). Mutually exclusive with --head-job-template.")
        public String headJobMachineType;

        @Option(names = {"--head-job-template"}, description = "Google Compute Engine instance template for the Nextflow head job. Specify either the template name (if in the same project) or the fully qualified reference (projects/PROJECT_ID/global/instanceTemplates/TEMPLATE_NAME). Mutually exclusive with --head-job-machine-type.")
        public String headJobInstanceTemplate;

        @Option(names = {"--compute-jobs-machine-type"}, split = ",", paramLabel = "<type>", description = "Comma-separated list of GCP machine types for compute jobs (e.g., n2-standard-8,c2-standard-4). Supports wildcard families (e.g., n2-*). Mutually exclusive with --compute-job-template.")
        public List<String> computeJobsMachineType;

        @Option(names = {"--compute-job-template"}, description = "Google Compute Engine instance template for pipeline compute jobs. Specify either the template name (if in the same project) or the fully qualified reference (projects/PROJECT_ID/global/instanceTemplates/TEMPLATE_NAME). Mutually exclusive with --compute-jobs-machine-type.")
        public String computeJobInstanceTemplate;
    }
}
