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

import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.model.AwsCredentialsMode;
import io.seqera.tower.model.AwsSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.util.Locale;

public class AwsProvider extends AbstractProvider<AwsSecurityKeys> {

    @ArgGroup(exclusive = false)
    public Keys keys;

    @Option(names = {"-r", "--assume-role-arn"}, description = "IAM role ARN to assume for accessing AWS resources. Allows cross-account access or privilege elevation. Must be a fully qualified ARN (e.g., arn:aws:iam::123456789012:role/RoleName).")
    String assumeRoleArn;

    @Option(names = {"--mode"}, description = "AWS credential mode: 'keys' (access key + secret key), 'role' (IAM role only) or 'workload-identity' (OIDC workload identity federation via sts:AssumeRoleWithWebIdentity, requires Identity Federation enabled). Default: keys. The mode cannot be changed after creation; on update, it defaults to the mode of the existing credentials.")
    String mode;

    // Mode of the existing credentials, used on update when '--mode' is not given
    private AwsCredentialsMode inheritedMode;

    @Option(names = {"--generate-external-id"}, description = "Generate a platform-managed External ID for the credential (used with IAM role ARN).", defaultValue = "false")
    boolean generateExternalId;

    public AwsProvider() {
        super(ProviderEnum.AWS);
    }

    @Override
    public AwsSecurityKeys securityKeys() {
        validate();

        AwsSecurityKeys result = new AwsSecurityKeys();

        if (getMode() != null) {
            result.mode(getMode());
        }

        if (keys != null) {
            result.accessKey(keys.accessKey).secretKey(keys.secretKey);
        }

        if (assumeRoleArn != null) {
            result.assumeRoleArn(assumeRoleArn);
        }

        return result;
    }

    @Override
    public boolean useExternalId() {
        AwsCredentialsMode mode = getMode();
        if (mode == AwsCredentialsMode.role) {
            return true;
        }
        if (mode == AwsCredentialsMode.workloadIdentity) {
            return false;
        }
        return generateExternalId && assumeRoleArn != null;
    }

    @Override
    public boolean fetchSetupDetails() {
        return useExternalId() || getMode() == AwsCredentialsMode.workloadIdentity;
    }

    public boolean hasMode() {
        return mode != null;
    }

    public void inheritMode(AwsCredentialsMode mode) {
        this.inheritedMode = mode;
    }

    private AwsCredentialsMode getMode() {
        if (mode == null) {
            return inheritedMode;
        }
        return switch (mode.toLowerCase(Locale.ROOT)) {
            case "keys" -> AwsCredentialsMode.keys;
            case "role" -> AwsCredentialsMode.role;
            case "workload-identity", "workloadidentity" -> AwsCredentialsMode.workloadIdentity;
            default -> throw new TowerRuntimeException(String.format("Invalid AWS credential mode '%s'. Allowed values: 'keys', 'role', 'workload-identity'.", mode));
        };
    }

    /**
     * How errors name the mode: the '--mode' option the user passed, or the mode inherited from the
     * existing credentials on update, so the message does not quote an option that was never typed.
     */
    private String modeForErrorMessage(String modeName) {
        return mode != null
                ? String.format("'--mode=%s'", modeName)
                : String.format("the existing credentials' %s mode", modeName);
    }

    private void validate() {
        AwsCredentialsMode mode = getMode();

        if (mode == AwsCredentialsMode.workloadIdentity) {
            String ref = modeForErrorMessage("workload-identity");
            if (keys != null && (keys.accessKey != null || keys.secretKey != null)) {
                throw new TowerRuntimeException(String.format("Options '--access-key' and '--secret-key' cannot be used with %s. Workload identity mode uses short-lived OIDC tokens without static credentials.", ref));
            }
            if (assumeRoleArn == null) {
                throw new TowerRuntimeException(String.format("Option '--assume-role-arn' is required when using %s.", ref));
            }
            if (generateExternalId) {
                throw new TowerRuntimeException(String.format("Option '--generate-external-id' cannot be used with %s.", ref));
            }
        }

        if (mode == AwsCredentialsMode.role) {
            String ref = modeForErrorMessage("role");
            if (keys != null && (keys.accessKey != null || keys.secretKey != null)) {
                throw new TowerRuntimeException(String.format("Options '--access-key' and '--secret-key' cannot be used with %s. Role mode uses IAM role assumption without static credentials.", ref));
            }
            if (assumeRoleArn == null) {
                throw new TowerRuntimeException(String.format("Option '--assume-role-arn' is required when using %s.", ref));
            }
        }

        if (generateExternalId && mode != AwsCredentialsMode.role && assumeRoleArn == null) {
            throw new TowerRuntimeException("Option '--generate-external-id' requires '--assume-role-arn' to be specified.");
        }
    }

    public static class Keys {

        @Option(names = {"-a", "--access-key"}, description = "AWS access key identifier. Part of AWS IAM credentials used for programmatic access to AWS services.")
        String accessKey;

        @Option(names = {"-s", "--secret-key"}, description = "AWS secret access key. Part of AWS IAM credentials used for programmatic access to AWS services. Keep this value secure.")
        String secretKey;
    }
}