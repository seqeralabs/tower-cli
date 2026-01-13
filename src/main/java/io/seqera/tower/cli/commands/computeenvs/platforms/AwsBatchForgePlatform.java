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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.ForgeConfig;
import io.seqera.tower.model.ForgeConfig.AllocStrategyEnum;
import io.seqera.tower.model.ForgeConfig.TypeEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class AwsBatchForgePlatform extends AbstractPlatform<AwsBatchConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be an S3 bucket path (e.g., s3://your-bucket/work).", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "AWS region where compute resources will be created (e.g., us-east-1, eu-west-1).", required = true)
    public String region;

    @Option(names = {"--max-cpus"}, description = "Maximum CPUs provisioned by Batch Forge. Defines the upper limit for auto-scaling compute capacity.", required = true)
    public Integer maxCpus;

    @Option(names = {"--provisioning-model"}, description = "Instance provisioning model. EC2 uses on-demand instances for reliability. SPOT uses interruptible instances for cost savings. Default: SPOT.", required = true, defaultValue = "SPOT")
    public TypeEnum provisioningModel;

    @Option(names = {"--no-ebs-auto-scale"}, description = "Disable EBS auto-expandable disk provisioning. When disabled, instances use fixed-size storage volumes.")
    public boolean noEbsAutoScale;

    @Option(names = {"--fusion"}, description = "DEPRECATED - Use '--fusion-v2' instead.")
    public boolean fusion;

    @Option(names = {"--fusion-v2"}, description = "Enable Fusion file system. Provides native access to S3 storage with low-latency I/O. Requires Wave containers. Default: false.")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Enable Wave containers. Allows access to private container repositories and on-demand container provisioning. Default: false.")
    public boolean wave;

    @Option(names = {"--fast-storage"}, description = "Enable NVMe instance storage. Provides high-performance local storage for faster I/O operations. Requires Fusion file system.")
    public boolean fastStorage;

    @Option(names = {"--snapshots"}, description = "Enable Fusion Snapshots. Automatically restores jobs interrupted by spot instance reclamation. Requires Fusion file system.")
    public boolean snapshots;

    @Option(names = {"--fargate"}, description = "Run Nextflow head job on Fargate. Enables serverless container execution for the orchestration process. Requires Fusion v2 and Spot provisioning model.")
    public boolean fargate;

    @Option(names = {"--gpu"}, description = "Enable GPU instances. Provisions GPU-enabled EC2 instances for compute-intensive workloads requiring hardware acceleration.")
    public boolean gpu;

    @Option(names = {"--allow-buckets"}, split = ",", paramLabel = "<bucket>", description = "Additional S3 buckets for read-write access. Comma-separated list of S3 bucket paths beyond the work directory. Format: s3://bucket-name or s3://bucket-name/path.")
    public List<String> allowBuckets;

    @Option(names = "--preserve-resources", description = "Preserve Batch Forge resources on deletion. Keeps AWS Batch compute environments and related resources when the compute environment is deleted from Seqera Platform.")
    public boolean preserveResources;

    @Option(names = {"--ecs-config"}, description = "Custom ECS agent configuration file. Appends custom parameters to /etc/ecs/ecs.config on each cluster node. Provide path to configuration file.")
    public Path ecsConfig;

    @ArgGroup(heading = "%nEFS filesystem options:%n", validate = false)
    public EfsFileSystem efs;
    @ArgGroup(heading = "%nFSX filesytem options:%n", validate = false)
    public FsxFileSystem fsx;
    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AwsBatchForgePlatform() {
        super(PlatformEnum.AWS_BATCH);
    }

    /**
     * Clean backend generated config
     *
     * @param config
     */
    public static void clean(AwsBatchConfig config) {
        //TODO differentiate between manual and forged config
        config.forgedResources(null);
        config.computeQueue(null);
        config.headQueue(null);
        config.volumes(null);
    }

    @Override
    public AwsBatchConfig computeConfig() throws ApiException, IOException {
        AwsBatchConfig config = new AwsBatchConfig()
                .region(region)
                .fusion2Enabled(isFusionV2Enabled())
                .waveEnabled(wave)
                .nvnmeStorageEnabled(fastStorage)
                .fusionSnapshots(snapshots)
                // Forge
                .forge(buildForge())
                // Advanced
                .cliPath(adv().cliPath)
                .executionRole(adv().batchExecutionRole)
                .computeJobRole(adv().computeJobRole)
                .headJobCpus(adv().headJobCpus)
                .headJobMemoryMb(adv().headJobMemoryMb)
                .headJobRole(adv().headJobRole);

                // Common
                config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

                return config;
    }

    private Boolean isFusionV2Enabled() throws TowerException {
        // TODO: delete this validation once wave is no longer a requirement for Fusion V2
        if (fusionV2 && !wave) throw new TowerException("Fusion v2 requires Wave service");
        return fusionV2;
    }

    private ForgeConfig buildForge() throws TowerException, IOException {

        // TODO: delete this once fusion v1 is completely removed
        if (fusion) {
            throw new TowerException("Fusion v1 is deprecated, please use '--fusion-v2' instead");
        }

        ForgeConfig forge = new ForgeConfig()
                .type(provisioningModel)
                .maxCpus(maxCpus)
                .ebsAutoScale(!noEbsAutoScale)
                .gpuEnabled(gpu)
                .allowBuckets(allowBuckets)
                .disposeOnDeletion(!preserveResources)
                .instanceTypes(adv().instanceTypes)
                .allocStrategy(adv().allocStrategy)
                .vpcId(adv().vpcId)
                .subnets(adv().subnets)
                .securityGroups(adv().securityGroups)
                .imageId(adv().amiId)
                .ec2KeyPair(adv().keyPair)
                .minCpus(adv().minCpus == null ? 0 : adv().minCpus)
                .ebsBlockSize(adv().ebsBlockSize)
                .ebsBootSize(adv().bootDiskSizeGb)
                .bidPercentage(adv().bidPercentage)
                .fargateHeadEnabled(fargate)
                .ecsConfig(FilesHelper.readString(ecsConfig));


        if (efs != null) {
            forge
                    .efsCreate(efs.createEfs)
                    .efsId(efs.efsId)
                    .efsMount(efs.efsMount);
        }

        if (fsx != null) {
            forge
                    .fsxName(fsx.fsxDns)
                    .fsxSize(fsx.fsxSize)
                    .fsxMount(fsx.fsxMount);
        }

        return forge;
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new AdvancedOptions();
        }
        return adv;
    }

    static class EfsFileSystem {

        @Option(names = {"--create-efs"}, description = "A OneZone EFS without backup will be created. EC2 instances can run on a different zone and inter-region transfer fees will be billed. If you want to remove transfer costs, restrict to only one subnet at advanced options.")
        public boolean createEfs;

        @Option(names = {"--efs-id"}, description = "Enter the EFS file system id e.g. fs-0123456789.")
        public String efsId;

        @Option(names = {"--efs-mount"}, description = "Enter the EFS mount path [default: pipeline work directory].")
        public String efsMount;

    }

    static class FsxFileSystem {

        @Option(names = {"--fsx-size"}, description = "Enter the FSx storage capacity in GB (minimum 1,200 GB or increments of 2,400 GB).")
        public Integer fsxSize;

        @Option(names = {"--fsx-dns"}, description = "Enter the FSx file system DNS name e.g. 'fs-0123456789.fsx.eu-west-1.amazonaws.com'.")
        public String fsxDns;

        @Option(names = {"--fsx-mount"}, description = "Enter the FSx mount path [default: pipeline work directory].")
        public String fsxMount;

    }

    public static class AdvancedOptions {
        @Option(names = {"--instance-types"}, split = ",", paramLabel = "<type>", description = "EC2 instance types for compute resources. Comma-separated list of instance families or types. Use 'optimal' for automatic selection of M4, C4, and R4 instances.")
        public List<String> instanceTypes;

        @Option(names = {"--alloc-strategy"}, description = "Instance allocation strategy. Controls how AWS Batch launches instances. BEST_FIT_PROGRESSIVE recommended for On-Demand. SPOT_CAPACITY_OPTIMIZED recommended for Spot instances.")
        public AllocStrategyEnum allocStrategy;

        @Option(names = {"--vpc-id"}, description = "VPC identifier. The Virtual Private Cloud where compute resources will be deployed.")
        public String vpcId;

        @Option(names = {"--subnets"}, split = ",", paramLabel = "<subnet>", description = "VPC subnets for compute resources. Comma-separated list of subnet IDs for network isolation and internet access control.")
        public List<String> subnets;

        @Option(names = {"--security-groups"}, split = ",", paramLabel = "<group>", description = "Security group IDs for network access control. Comma-separated list defining firewall rules for EC2 compute nodes.")
        public List<String> securityGroups;

        @Option(names = {"--ami-id"}, description = "Custom AMI identifier. Must be AWS Linux 2 ECS-optimized image meeting compute resource specifications. Default: latest approved Amazon ECS-optimized AMI.")
        public String amiId;

        @Option(names = {"--key-pair"}, description = "EC2 key pair name for SSH access. Enables remote access to compute nodes for debugging and maintenance.")
        public String keyPair;

        @Option(names = {"--min-cpus"}, description = "Minimum CPUs to keep provisioned. These CPUs remain active continuously and incur costs regardless of workload activity. Default: 0.")
        public Integer minCpus;

        @Option(names = {"--boot-disk-size"}, description = "Boot disk size in GB. Controls the root volume size for EC2 instances. Default: 50 GB.")
        public Integer bootDiskSizeGb;

        @Option(names = {"--head-job-cpus"}, description = "Number of CPUs allocated to the Nextflow head job. Controls the compute resources for the main workflow orchestration process.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "Memory allocation for the Nextflow head job in megabytes. Determines available memory for workflow orchestration.")
        public Integer headJobMemoryMb;

        @Option(names = {"--head-job-role"}, description = "IAM role ARN to grant fine-grained permissions to the Nextflow head job. Enables secure access to AWS resources.")
        public String headJobRole;

        @Option(names = {"--compute-job-role"}, description = "IAM role ARN to grant fine-grained permissions to Nextflow compute jobs. Controls access for individual pipeline tasks.")
        public String computeJobRole;
        @Option(names = {"--batch-execution-role"}, description = "IAM role ARN for ECS task execution. Grants Amazon ECS containers permission to make AWS API calls on your behalf.")
        public String batchExecutionRole;

        @Option(names = {"--ebs-blocksize"}, description = "Initial EBS auto-expandable volume size in GB. Additional blocks of this size are added automatically when storage runs low. Default: 50 GB.")
        public Integer ebsBlockSize;

        @Option(names = {"--bid-percentage"}, description = "Maximum Spot instance price as percentage of On-Demand price. Controls cost ceiling for Spot instances. You pay the market price up to this maximum. Default: 100%%.")
        public Integer bidPercentage;

        @Option(names = {"--cli-path"}, description = "AWS CLI installation path on EC2 instances. Specify custom path if AWS CLI is installed in non-standard location.")
        public String cliPath;

    }

}
