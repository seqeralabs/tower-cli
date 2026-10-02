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
import io.seqera.tower.model.AwsCloudConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.SchedConfig;
import io.seqera.tower.model.SchedConfigPool;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;

public class AwsCloudPlatform extends AbstractPlatform<AwsCloudConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be an S3 bucket path (e.g., s3://your-bucket/work). Credentials must have read-write access.", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "AWS region where EC2 instances will be launched (e.g., us-east-1, eu-west-1).", required = true)
    public String region;

    @Option(names = {"--allow-buckets"}, description = "S3 buckets that the compute environment can access. Comma-separated list of S3 bucket names or paths to grant read-write permissions for workflow data.", split = ",")
    public List<String> allowBuckets;

    @ArgGroup(heading = "%nScheduler options:%n", validate = false)
    public SchedOptions sched;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AwsCloudPlatform() {
        super(PlatformEnum.AWS_CLOUD);
    }

    @Option(names = {"--fusion-metrics-collection"}, negatable = true, description = "Send Fusion metrics to Seqera for this compute environment. Fusion always generates the metrics; this only controls whether they are collected and sent to Seqera. Only valid when Fusion is enabled. If unset, Platform applies its default.")
    public Boolean fusionMetricsCollection;

    @Override
    public Boolean fusionMetricsCollectionEnabled() {
        return fusionMetricsCollection;
    }

    @Override
    public AwsCloudConfig computeConfig() throws ApiException, IOException {
        AwsCloudConfig config = new AwsCloudConfig();

        config
                .waveEnabled(true)
                .fusion2Enabled(true)
                .schedEnabled(sched != null && Boolean.TRUE.equals(sched.schedEnabled))

                // Main
                .region(region)
                .allowBuckets(allowBuckets);

        if (sched != null) {
            SchedConfig schedConfig = new SchedConfig()
                    .provisioningModel(sched.provisioningModel)
                    .machineTypes(sched.machineTypes)
                    .predictionModel(sched.predictionModel)
                    .nvmeEnabled(sched.nvmeEnabled)
                    .maxCpusPerUser(sched.maxCpusPerUser)
                    .maxSpotAttempts(sched.maxSpotAttempts)
                    .backendStrategy(sched.backendStrategy)
                    .pool(sched.warmPool());
            config.schedConfig(schedConfig);
        }

        // Advanced
        if (adv != null) {
            if (adv.ebsKmsKeyId != null && !Boolean.TRUE.equals(adv.ebsEncrypted)) {
                throw new TowerRuntimeException("EBS KMS key requires EBS encryption to be enabled (--ebs-encryption).");
            }

            if (adv.subnetId != null && adv.subnetIds != null) {
                throw new TowerRuntimeException("Options --subnet-id and --subnet-ids are mutually exclusive; use --subnet-ids.");
            }

            config
                    .instanceType(adv.instanceType)
                    .imageId(adv.imageId)
                    .arm64Enabled(adv.arm64Enabled)
                    .ec2KeyPair(adv.ec2KeyPair)
                    .ebsBootSize(adv.ebsBootSize)
                    .ebsEncrypted(adv.ebsEncrypted)
                    .ebsKmsKeyId(adv.ebsKmsKeyId)
                    .secretsKmsKeyId(adv.secretsKmsKeyId)
                    .instanceProfileArn(adv.instanceProfileArn)
                    .vpcId(adv.vpcId)
                    .subnetId(adv.subnetId)
                    .subnetIds(adv.subnetIds)
                    .securityGroups(adv.securityGroups);
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

        @Option(names = {"--sched-machine-types"}, description = "EC2 instance types for compute nodes managed by the Seqera scheduler. Comma-separated list (e.g., m5.xlarge,c5.2xlarge). Leave empty to let the scheduler select the most cost-effective types.", split = ",")
        public List<String> machineTypes;

        @Option(names = {"--prediction-model"}, description = "Model the Seqera scheduler uses to predict task resource requirements. Suggested values: none, qr/v1, qr/v2, qr/v3. If absent, the scheduler default (none) applies.")
        public String predictionModel;

        @Option(names = {"--nvme-storage"}, description = "Restrict the Seqera scheduler to EC2 instance types that provide local SSD (NVMe) storage for faster I/O.")
        public Boolean nvmeEnabled;

        @Option(names = {"--max-cpus-per-user"}, description = "Maximum concurrent vCPUs a single user may use across their runs on this compute environment. Must be a positive integer. If absent, there is no limit.")
        public Integer maxCpusPerUser;

        @Option(names = {"--max-spot-attempts"}, description = "Total spot attempts for a task, including the first, before giving up on spot capacity (1-10). With SPOT_FIRST, the task then falls back to on-demand. Only valid with the SPOT and SPOT_FIRST provisioning models.")
        public Integer maxSpotAttempts;

        @Option(names = {"--backend-strategy"}, description = "Backend the Seqera scheduler uses to run tasks. ECS delegates task execution to AWS ECS; EC2 runs tasks directly on EC2 instances. Valid values: ECS, EC2.")
        public SchedConfig.BackendStrategyEnum backendStrategy;

        @Option(names = {"--warm-pool"}, description = "Keep a pool of idle VMs ready to absorb incoming tasks with minimal start latency. Requires --warm-pool-size. Only applies with --backend-strategy EC2.")
        public Boolean warmPoolEnabled;

        @Option(names = {"--warm-pool-size"}, description = "Number of idle VMs to keep in the warm pool. Must be greater than zero.")
        public Integer warmPoolSize;

        @Option(names = {"--warm-pool-scale-to-zero"}, paramLabel = "<seconds>", description = "Seconds of inactivity after which the warm pool scales to zero. Set to 0 to never scale to zero.")
        public Integer warmPoolScaleToZeroSecs;

        SchedConfigPool warmPool() {
            if (warmPoolEnabled == null && warmPoolSize == null && warmPoolScaleToZeroSecs == null) {
                return null;
            }
            if (Boolean.TRUE.equals(warmPoolEnabled) && (warmPoolSize == null || warmPoolSize < 1)) {
                throw new TowerRuntimeException("Option --warm-pool requires --warm-pool-size greater than zero.");
            }
            return new SchedConfigPool()
                    .enabled(warmPoolEnabled)
                    .desiredWarm(warmPoolSize)
                    .scaleToZeroSecs(warmPoolScaleToZeroSecs);
        }
    }

    public static class AdvancedOptions {
        @Option(names = {"--arm64"}, description = "Enable ARM64 (Graviton) architecture EC2 instances to run compute jobs. Provides cost-effective compute with comparable performance to x86.")
        public Boolean arm64Enabled;

        @Option(names = {"--boot-disk-size"}, description = "EC2 instance boot disk size in GB. Controls the root volume size for compute instances. If absent, Platform defaults to 50 GB gp3 volume.")
        public Integer ebsBootSize;

        @Option(names = {"--ebs-encryption"}, description = "Encrypt the boot EBS volume of provisioned instances. Defaults to false if not specified.")
        public Boolean ebsEncrypted;

        @Option(names = {"--ebs-kms-key"}, description = "KMS key ARN used to encrypt the boot EBS volume. Only applied when EBS encryption is enabled (--ebs-encryption). When omitted, the account/region default EBS encryption key is used.")
        public String ebsKmsKeyId;

        @Option(names = {"--secrets-kms-key"}, description = "Customer-managed KMS key used to encrypt the temporary Secrets Manager secrets created for runs that use pipeline secrets. Accepts a key ARN or a key id. When omitted, the AWS-managed default Secrets Manager key is used.")
        public String secretsKmsKeyId;

        @Option(names = {"--ec2-key-pair"}, description = "EC2 key pair name for SSH access to running instances. The key pair must already exist in the specified region.")
        public String ec2KeyPair;

        @Option(names = {"--image-id"}, description = "AMI ID for launching EC2 instances. If omitted, Seqera-maintained default AMI is used. Use Seqera AMIs for best performance.")
        public String imageId;

        @Option(names = {"--instance-profile-arn"}, description = "IAM instance profile ARN used by EC2 instances to assume roles. If unspecified, Seqera provisions an ARN with sufficient permissions.")
        public String instanceProfileArn;

        @Option(names = {"--instance-type"}, description = "EC2 instance type (e.g., t3.medium, m5.large). If omitted, a default instance type is used.")
        public String instanceType;

        @Option(names = {"--security-groups"}, description = "Security group IDs for network access control. Comma-separated list defining firewall rules for EC2 instances.", split = ",")
        public List<String> securityGroups;

        @Deprecated
        @Option(names = {"--subnet-id"}, description = "DEPRECATED - Use '--subnet-ids' instead. VPC subnet ID for instance placement. Determines network isolation and internet access configuration.")
        public String subnetId;

        @Option(names = {"--subnet-ids"}, description = "VPC subnet IDs for instance placement. Comma-separated list; the first subnet is used for basic placement while Intelligent Compute may use all of them. Mutually exclusive with --subnet-id.", split = ",")
        public List<String> subnetIds;

        @Option(names = {"--vpc-id"}, description = "VPC ID used to scope subnet and security-group selection. Determines the network in which EC2 instances are launched.")
        public String vpcId;
    }
}
