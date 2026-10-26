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
import io.seqera.tower.model.SeqeraComputeCloudInstanceTypeSize;
import io.seqera.tower.model.SeqeraComputeConfig;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;

public class SeqeraComputePlatform extends AbstractPlatform<SeqeraComputeConfig> {

    @CommandLine.Option(names = {"--work-dir"}, description = "Work directory suffix relative to the S3 bucket that will be created by Seqera Compute.")
    public String workDir;

    @CommandLine.Option(names = {"-r", "--region"}, description = "AWS region.", required = true)
    public String region;

    @CommandLine.Option(names = {"--instance-type-size"}, description = "Studios instance size, controlling compute resources and capabilities. Options: ${COMPLETION-CANDIDATES}. Free-tier organizations are limited to SMALL.")
    public SeqeraComputeCloudInstanceTypeSize instanceTypeSize;

    @CommandLine.Option(names = {"--data-retention-policy"}, description = "Apply the automatic data retention policy to the S3 bucket created by Seqera Compute. Intermediate files are deleted after 28 days to manage storage cost.")
    public Boolean dataRetentionPolicy;

    @CommandLine.Option(names = {"--allow-buckets"}, split = ",", paramLabel = "<bucket>", description = "S3 buckets or paths this compute environment can read and write, other than the work directory. Comma-separated list of up to 10 entries, in the format s3://bucket-name or s3://bucket-name/path. Each bucket needs a bucket policy, shown by 'tw compute-envs view' once the compute environment is available. Can't be changed after creation.")
    public List<String> allowBuckets;

    public SeqeraComputePlatform() {
        super(PlatformEnum.SEQERACOMPUTE_PLATFORM);
    }

    @Override
    public SeqeraComputeConfig computeConfig() throws ApiException, IOException {
        SeqeraComputeConfig config = new SeqeraComputeConfig();

        // NOTE: even though 'SeqeraComputeConfig' extends 'AwsBatchConfig', most
        // settings will automatically be configured by seqera compute and can't
        // be overridden.

        config.region(region)
                .defaultDataRetentionPolicy(dataRetentionPolicy)
                .allowBuckets(allowBuckets);

        if (instanceTypeSize != null) {
            config.instanceTypeSize(instanceTypeSize);
        }

        // Common
        config.preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }
}
