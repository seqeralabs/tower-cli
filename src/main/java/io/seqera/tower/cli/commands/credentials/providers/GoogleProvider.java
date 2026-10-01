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
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.GoogleSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public class GoogleProvider extends AbstractProvider<GoogleSecurityKeys> {

    private static final Pattern SA_EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.iam\\.gserviceaccount\\.com$");

    private static final Pattern WIF_PROVIDER_PATTERN = Pattern.compile(
            "^projects/[^/]+/locations/global/workloadIdentityPools/[^/]+/providers/[^/]+$");

    @Option(names = {"-k", "--key"}, description = "Path to JSON file containing Google Cloud service account key. Download from Google Cloud Console IAM & Admin > Service Accounts.")
    public Path serviceAccountKey;

    @Option(names = {"--mode"}, description = "Google credential mode: 'service-account-key' (JSON key file) or 'workload-identity' (WIF with OIDC tokens, requires Identity Federation enabled). Default: service-account-key. On update, it defaults to the mode of the existing credentials.")
    String mode;

    // Whether the existing credentials use workload identity, used on update when '--mode' is not given
    private boolean inheritedWorkloadIdentity;

    @Option(names = {"--service-account-email"}, description = "The email address of the Google Cloud service account to impersonate (required for workload-identity mode).")
    String serviceAccountEmail;

    @Option(names = {"--workload-identity-provider"}, description = "The full resource name of the Workload Identity Pool provider. Format: projects/{PROJECT}/locations/global/workloadIdentityPools/{POOL}/providers/{PROVIDER}")
    String workloadIdentityProvider;

    @Option(names = {"--token-audience"}, description = "Optional. The intended audience for the OIDC token. If not specified, defaults to //iam.googleapis.com/<workload identity provider>.")
    String tokenAudience;

    public GoogleProvider() {
        super(ProviderEnum.GOOGLE);
    }

    @Override
    public GoogleSecurityKeys securityKeys() throws IOException {
        validate();

        GoogleSecurityKeys result = new GoogleSecurityKeys();

        if (isWorkloadIdentityMode()) {
            result.serviceAccountEmail(serviceAccountEmail);
            result.workloadIdentityProvider(workloadIdentityProvider);
            if (tokenAudience != null) {
                result.tokenAudience(tokenAudience);
            }
        } else {
            result.data(FilesHelper.readString(serviceAccountKey));
        }

        return result;
    }

    public boolean hasMode() {
        return mode != null;
    }

    public void inheritWorkloadIdentity(boolean workloadIdentity) {
        this.inheritedWorkloadIdentity = workloadIdentity;
    }

    private boolean isWorkloadIdentityMode() {
        if (mode == null) {
            return inheritedWorkloadIdentity;
        }
        return switch (mode.toLowerCase(Locale.ROOT)) {
            case "service-account-key" -> false;
            case "workload-identity", "workloadidentity" -> true;
            default -> throw new TowerRuntimeException(
                    String.format("Invalid Google credential mode '%s'. Allowed values: 'service-account-key', 'workload-identity'.", mode));
        };
    }

    /**
     * How errors name workload identity mode: the '--mode' option the user passed, or the mode inherited
     * from the existing credentials on update, so the message does not quote an option that was never typed.
     */
    private String workloadIdentityForErrorMessage() {
        return mode != null ? "'--mode=workload-identity'" : "the existing credentials' workload-identity mode";
    }

    private void validate() {
        if (isWorkloadIdentityMode()) {
            String ref = workloadIdentityForErrorMessage();
            if (serviceAccountKey != null) {
                String hint = mode != null ? "" : " To switch these credentials to a key file, add '--mode=service-account-key'.";
                throw new TowerRuntimeException(String.format("Option '--key' cannot be used with %s. Workload Identity mode uses federated authentication without a key file.%s", ref, hint));
            }
            if (serviceAccountEmail == null) {
                throw new TowerRuntimeException(String.format("Option '--service-account-email' is required when using %s.", ref));
            }
            if (!SA_EMAIL_PATTERN.matcher(serviceAccountEmail).matches()) {
                throw new TowerRuntimeException("Invalid service account email format. Expected format: <name>@<project>.iam.gserviceaccount.com");
            }
            if (workloadIdentityProvider == null) {
                throw new TowerRuntimeException(String.format("Option '--workload-identity-provider' is required when using %s.", ref));
            }
            if (!WIF_PROVIDER_PATTERN.matcher(workloadIdentityProvider).matches()) {
                throw new TowerRuntimeException("Invalid Workload Identity Provider format. Expected: projects/{PROJECT_NUMBER}/locations/global/workloadIdentityPools/{POOL}/providers/{PROVIDER}");
            }
        } else {
            if (serviceAccountEmail != null || workloadIdentityProvider != null || tokenAudience != null) {
                throw new TowerRuntimeException("Options '--service-account-email', '--workload-identity-provider', and '--token-audience' can only be used with '--mode=workload-identity'.");
            }
            if (serviceAccountKey == null) {
                throw new TowerRuntimeException("Option '--key' is required when using service account key mode.");
            }
        }
    }
}
