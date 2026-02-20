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

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AwsSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class AwsProvider extends AbstractProvider<AwsSecurityKeys> {

    @ArgGroup(exclusive = false)
    public Keys keys;
    @Option(names = {"-r", "--assume-role-arn"}, description = "IAM role ARN to assume for accessing AWS resources. Allows cross-account access or privilege elevation. Must be a fully qualified ARN (e.g., arn:aws:iam::123456789012:role/RoleName).")
    String assumeRoleArn;

    public AwsProvider() {
        super(ProviderEnum.AWS);
    }

    @Override
    public AwsSecurityKeys securityKeys() {
        AwsSecurityKeys result = new AwsSecurityKeys();
        if (keys != null) {
            result.accessKey(keys.accessKey).secretKey(keys.secretKey);
        }

        if (assumeRoleArn != null) {
            result.assumeRoleArn(assumeRoleArn);
        }

        return result;
    }

    public static class Keys {

        @Option(names = {"-a", "--access-key"}, description = "AWS access key identifier. Part of AWS IAM credentials used for programmatic access to AWS services.")
        String accessKey;

        @Option(names = {"-s", "--secret-key"}, description = "AWS secret access key. Part of AWS IAM credentials used for programmatic access to AWS services. Keep this value secure.")
        String secretKey;
    }
}
