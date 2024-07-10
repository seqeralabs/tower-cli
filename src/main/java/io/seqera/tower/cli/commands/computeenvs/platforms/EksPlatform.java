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
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.EksComputeConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class EksPlatform extends AbstractPlatform<EksComputeConfig> {

    @Option(names = {"-r", "--region"}, description = "AWS region.", required = true)
    public String region;

    @Option(names = {"--cluster-name"}, description = "The AWS EKS cluster name.", required = true)
    public String clusterName;

    @Option(names = {"--namespace"}, description = "Namespace.", required = true)
    public String namespace;

    @Option(names = {"--head-account"}, description = "Head service account.", required = true)
    public String headAccount;

    @Option(names = {"--storage-claim"}, description = "Storage claim name.")
    public String storageClaim;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public K8sPlatform.AdvancedOptions adv;

    public EksPlatform() {
        super(PlatformEnum.EKS_PLATFORM);
    }

    @Override
    public EksComputeConfig computeConfig() throws ApiException, IOException {
        EksComputeConfig config = new EksComputeConfig();
        config
                .region(region)
                .clusterName(clusterName)
                .namespace(namespace)
                .headServiceAccount(headAccount)
                .storageClaimName(storageClaim)

                // Advance
                .storageMountPath(adv().storageMount)
                .computeServiceAccount(adv().computeAccount)
                .podCleanup(adv().podCleanup)
                .headPodSpec(FilesHelper.readString(adv().headPodSpec))
                .servicePodSpec(FilesHelper.readString(adv().servicePodSpec))

                // Common
                .workDir(workDir)

                // Stagging
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .environment(environmentVariables());

        return config;
    }

    private K8sPlatform.AdvancedOptions adv() {
        if (adv == null) {
            return new K8sPlatform.AdvancedOptions();
        }
        return adv;
    }
}
