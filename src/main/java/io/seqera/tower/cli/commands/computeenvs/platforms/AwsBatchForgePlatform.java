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
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.ForgeConfig;
import io.seqera.tower.model.ForgeConfig.AllocStrategyEnum;
import io.seqera.tower.model.ForgeConfig.TypeEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;

public class AwsBatchForgePlatform extends AbstractPlatform<AwsBatchConfig> {

    @Option(names = {"-r", "--region"}, description = "AWS region.", required = true)
    public String region;

    @Option(names = {"--max-cpus"}, description = "The maximum number of CPUs provisioned in this environment.", required = true)
    public Integer maxCpus;

    @Option(names = {"--provisioning-model"}, description = "VMs provisioning model. 'EC2' deploys uninterruptible Ec2 instances. 'SPOT' uses interruptible Ec2 instances.", required = true, defaultValue = "SPOT")
    public TypeEnum provisioningModel;

    @Option(names = {"--no-ebs-auto-scale"}, description = "Disable the provisioning of EBS auto-expandable disk.")
    public boolean noEbsAutoScale;

    @Option(names = {"--fusion"}, description = "With Fusion enabled, S3 buckets specified in the Pipeline work directory and Allowed S3 Buckets fields will be accessible in the compute nodes storage using the file path /fusion/s3/BUCKET_NAME.")
    public boolean fusion;

    @Option(names = {"--gpu"}, description = "Deploys GPU enabled Ec2 instances.")
    public boolean gpu;

    @Option(names = {"--allow-buckets"}, split = ",", paramLabel = "<bucket>", description = "Comma-separated list of S3 buckets or paths other than pipeline work directory that should be granted read-write permission from this environment.")
    public List<String> allowBuckets;

    @Option(names = "--preserve-resources", description = "Enable this if you want to preserve the Batch compute resources created by Tower independently from the lifecycle of this compute environment.")
    public boolean preserveResources;

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
        return new AwsBatchConfig()
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .region(region)

                // Forge
                .forge(buildForge())

                // Advanced
                .cliPath(adv().cliPath)
                .computeJobRole(adv().computeJobRole)
                .headJobCpus(adv().headJobCpus)
                .headJobMemoryMb(adv().headJobMemoryMb)
                .headJobRole(adv().headJobRole);
    }

    private ForgeConfig buildForge() {
        ForgeConfig forge = new ForgeConfig()
                .type(provisioningModel)
                .maxCpus(maxCpus)
                .ebsAutoScale(!noEbsAutoScale)
                .fusionEnabled(fusion)
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
                .bidPercentage(adv().bidPercentage);

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
        @Option(names = {"--instance-types"}, split = ",", paramLabel = "<type>", description = "Specify the instance types to be used to carry out the computation. You can specify one or more family or instance type. The option 'optimal' chooses the best fit of M4, C4, and R4 instance types available in the region.")
        public List<String> instanceTypes;

        @Option(names = {"--alloc-strategy"}, description = "Allocation Strategies allow you to choose how Batch launches instances on your behalf. AWS recommends BEST_FIT_PROGRESSIVE for On-Demand CEs and SPOT_CAPACITY_OPTIMIZED for Spot CEs.")
        public AllocStrategyEnum allocStrategy;

        @Option(names = {"--vpc-id"}, description = "VPC identifier.")
        public String vpcId;

        @Option(names = {"--subnets"}, split = ",", paramLabel = "<subnet>", description = "Comma-separated list of one or more subnets in your VPC that can be used to isolate the EC2 resources from each other or from the Internet.")
        public List<String> subnets;

        @Option(names = {"--security-groups"}, split = ",", paramLabel = "<group>", description = "Comma-separated list of one or more security groups that defines a set of firewall rules to control the traffic for your EC2 compute nodes.")
        public List<String> securityGroups;

        @Option(names = {"--ami-id"}, description = "Ths option allows you to use your own AMI. Note however it must be an AWS Linux-2 ECS-optimised image and meet the compute resource AMI specification [default: latest approved version of the Amazon ECS-optimized AMI].")
        public String amiId;

        @Option(names = {"--key-pair"}, description = "The EC2 key pair to be installed in the compute nodes to access via SSH.")
        public String keyPair;

        @Option(names = {"--min-cpus"}, description = "The minimum number of CPUs provisioned in this environment that will remain active and you will be billed regardless of whether you are running any workloads.")
        public Integer minCpus;

        @Option(names = {"--head-job-cpus"}, description = "The number of CPUs to be allocated for the Nextflow runner job.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "The number of MiB of memory reserved for the Nextflow runner job.")
        public Integer headJobMemoryMb;

        @Option(names = {"--head-job-role"}, description = "IAM role to fine-grained control permissions for the Nextflow runner job.")
        public String headJobRole;

        @Option(names = {"--compute-job-role"}, description = "IAM role to fine-grained control permissions for jobs submitted by Nextflow.")
        public String computeJobRole;

        @Option(names = {"--ebs-blocksize"}, description = "This field controls the initial size of the EBS auto-expandable volume. New blocks of the same size are added as necessary when the volume is running out of free space [default: 50 GB].")
        public Integer ebsBlockSize;

        @Option(names = {"--bid-percentage"}, description = "The maximum percentage that a Spot Instance price can be when compared with the On-Demand price for that instance type before instances are launched. For example, if your maximum percentage is 20%%, then the Spot price must be less than 20%% of the current On-Demand price for that Amazon EC2 instance. You always pay the lowest (market) price and never more than your maximum percentage [default: 100%% of the On-Demand price].")
        public Integer bidPercentage;

        @Option(names = {"--cli-path"}, description = "Nextflow requires the AWS CLI installed in the Ec2 instances. Use this field to specify the path.")
        public String cliPath;

    }

}
