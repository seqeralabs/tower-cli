package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AwsSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class AwsProvider extends AbstractProvider<AwsSecurityKeys> {

    @ArgGroup(exclusive = false)
    public Keys keys;

    public static class Keys {

        @Option(names = {"--access-key"}, required = true)
        String accessKey;

        @Option(names = {"--secret-key"}, required = true)
        String secretKey;
    }

    @Option(names = {"--assume-role-arn"})
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
}
