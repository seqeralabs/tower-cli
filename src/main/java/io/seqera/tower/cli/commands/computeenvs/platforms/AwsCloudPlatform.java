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
import io.seqera.tower.model.AwsCloudConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
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

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AwsCloudPlatform() {
        super(PlatformEnum.AWS_CLOUD);
    }

    @Override
    public AwsCloudConfig computeConfig() throws ApiException, IOException {
        AwsCloudConfig config = new AwsCloudConfig();

        config
                .waveEnabled(true)
                .fusion2Enabled(true)

                // Main
                .region(region)
                .allowBuckets(allowBuckets);

        // Advanced
        if (adv != null) {
            config
                    .instanceType(adv.instanceType)
                    .imageId(adv.imageId)
                    .arm64Enabled(adv.arm64Enabled)
                    .ec2KeyPair(adv.ec2KeyPair)
                    .ebsBootSize(adv.ebsBootSize)
                    .instanceProfileArn(adv.instanceProfileArn)
                    .subnetId(adv.subnetId)
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

    public static class AdvancedOptions {
        @Option(names = {"--arm64"}, description = "Enable ARM64 (Graviton) architecture EC2 instances to run compute jobs. Provides cost-effective compute with comparable performance to x86.")
        public Boolean arm64Enabled;

        @Option(names = {"--boot-disk-size"}, description = "EC2 instance boot disk size in GB. Controls the root volume size for compute instances. Default: 50 GB gp3 volume.")
        public Integer ebsBootSize;

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

        @Option(names = {"--subnet-id"}, description = "VPC subnet ID for instance placement. Determines network isolation and internet access configuration.")
        public String subnetId;
    }
}
