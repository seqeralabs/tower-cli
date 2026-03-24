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
import io.seqera.tower.model.GoogleBatchConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GoogleBatchPlatform extends AbstractPlatform<GoogleBatchConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be a Google Cloud Storage bucket path (e.g., gs://your-bucket/work).", required = true)
    public String workDir;

    @Option(names = {"-l", "--location"}, description = "Google Cloud region where job executions are deployed to Google Batch API (e.g., us-central1, europe-west1).", required = true)
    public String location;

    @Option(names = {"--spot"}, description = "Use Spot virtual machines. Enables cost-effective preemptible instances for compute workloads. Spot VMs may be interrupted when capacity is needed.")
    public Boolean spot;

    @Option(names = {"--fusion-v2"}, description = "Enable Fusion file system. Provides native access to Google Cloud Storage with low-latency I/O. Requires Wave containers.")
    public boolean fusionV2;

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

        config
                .fusion2Enabled(fusionV2)
                .waveEnabled(wave)

                // Main
                .location(location)
                .spot(spot);

        // Advanced
        if (adv != null) {
            config
                .usePrivateAddress(adv.usePrivateAddress)
                .bootDiskSizeGb(adv.bootDiskSizeGb)
                .headJobCpus(adv.headJobCpus)
                .headJobMemoryMb(adv.headJobMemoryMb)
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

    public static class AdvancedOptions {
        @Option(names = {"--use-private-address"}, description = "Do not attach a public IP address to VM instances. When enabled, only Google internal services are accessible. Requires Cloud NAT for external access.")
        public Boolean usePrivateAddress;

        @Option(names = {"--boot-disk-size"}, description = "Boot disk size in GB. Controls the root volume size for compute instances. If absent, Platform defaults to 50 GB.")
        public Integer bootDiskSizeGb;

        @Option(names = {"--head-job-cpus"}, description = "Number of CPUs allocated to the Nextflow head job. Controls the compute resources for the main workflow orchestration process.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "Memory allocation for the Nextflow head job in megabytes. Value must be a multiple of 256 MiB and from 0.5 GB to 8 GB per CPU.")
        public Integer headJobMemoryMb;

        @Option(names = {"--service-account-email"}, description = "Google Cloud service account email for pipeline execution. Grants fine-grained IAM permissions to Nextflow jobs.")
        public String serviceAccountEmail;

        @Option(names = {"--head-job-template"}, description = "Google Compute Engine instance template for the Nextflow head job. Specify either the template name (if in the same project) or the fully qualified reference (projects/PROJECT_ID/global/instanceTemplates/TEMPLATE_NAME).")
        public String headJobInstanceTemplate;

        @Option(names = {"--compute-job-template"}, description = "Google Compute Engine instance template for pipeline compute jobs. Specify either the template name (if in the same project) or the fully qualified reference (projects/PROJECT_ID/global/instanceTemplates/TEMPLATE_NAME).")
        public String computeJobInstanceTemplate;
    }
}
