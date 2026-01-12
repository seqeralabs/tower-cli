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

    @Option(names = {"--work-dir"}, description = "Work directory.", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "AWS region.", required = true)
    public String region;

    @Option(names = {"--allow-buckets"}, description = "S3 buckets that the compute environment can access.", split = ",")
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
        @Option(names = {"--arm64"}, description = "Use ARM64 (Graviton) based instances.")
        public Boolean arm64Enabled;

        @Option(names = {"--boot-disk-size"}, description = "Enter the boot disk size in GB.")
        public Integer ebsBootSize;

        @Option(names = {"--ec2-key-pair"}, description = "EC2 key pair to enable SSH connectivity to running instances.")
        public String ec2KeyPair;

        @Option(names = {"--image-id"}, description = "AMI ID for the EC2 instance.")
        public String imageId;

        @Option(names = {"--instance-profile-arn"}, description = "IAM instance profile ARN for the EC2 instance.")
        public String instanceProfileArn;

        @Option(names = {"--instance-type"}, description = "EC2 instance type.")
        public String instanceType;

        @Option(names = {"--security-groups"}, description = "Security group IDs for network access control.", split = ",")
        public List<String> securityGroups;

        @Option(names = {"--subnet-id"}, description = "VPC subnet ID for instance placement.")
        public String subnetId;
    }
}
