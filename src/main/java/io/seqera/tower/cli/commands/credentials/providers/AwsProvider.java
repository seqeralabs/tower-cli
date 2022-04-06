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

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AwsSecurityKeys;
import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class AwsProvider extends AbstractProvider<AwsSecurityKeys> {

    @ArgGroup(exclusive = false)
    public Keys keys;
    @Option(names = {"-r", "--assume-role-arn"}, description = "The IAM role to access the AWS resources. It should be a fully qualified AWS role ARN.")
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

        @Option(names = {"-a", "--access-key"}, description = "The AWS access key required to access the desired service.")
        String accessKey;

        @Option(names = {"-s", "--secret-key"}, description = "The AWS secret key required to access the desired service.")
        String secretKey;
    }
}
