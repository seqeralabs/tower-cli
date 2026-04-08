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

import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.GoogleSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class GoogleProvider extends AbstractProvider<GoogleSecurityKeys> {

    private static final Pattern SA_EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.iam\\.gserviceaccount\\.com$");

    private static final Pattern WIF_PROVIDER_PATTERN = Pattern.compile(
            "^projects/[^/]+/locations/global/workloadIdentityPools/[^/]+/providers/[^/]+$");

    @Option(names = {"-k", "--key"}, description = "Path to JSON file containing Google Cloud service account key. Download from Google Cloud Console IAM & Admin > Service Accounts.")
    public Path serviceAccountKey;

    @Option(names = {"--mode"}, description = "Google credential mode: 'service-account-key' (JSON key file) or 'workload-identity' (WIF with OIDC tokens). Default: service-account-key.")
    String mode;

    @Option(names = {"--service-account-email"}, description = "The email address of the Google Cloud service account to impersonate (required for workload-identity mode).")
    String serviceAccountEmail;

    @Option(names = {"--workload-identity-provider"}, description = "The full resource name of the Workload Identity Pool provider. Format: projects/{PROJECT}/locations/global/workloadIdentityPools/{POOL}/providers/{PROVIDER}")
    String workloadIdentityProvider;

    @Option(names = {"--token-audience"}, description = "Optional. The intended audience for the OIDC token. If not specified, defaults to the Workload Identity Provider resource name.")
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

    private boolean isWorkloadIdentityMode() {
        if (mode == null) {
            return false;
        }
        return switch (mode.toLowerCase()) {
            case "service-account-key" -> false;
            case "workload-identity" -> true;
            default -> throw new IllegalArgumentException(
                    String.format("Invalid Google credential mode '%s'. Allowed values: 'service-account-key', 'workload-identity'.", mode));
        };
    }

    private void validate() {
        if (isWorkloadIdentityMode()) {
            if (serviceAccountKey != null) {
                throw new IllegalArgumentException("Option '--key' cannot be used with '--mode=workload-identity'. Workload Identity mode uses federated authentication without a key file.");
            }
            if (serviceAccountEmail == null) {
                throw new IllegalArgumentException("Option '--service-account-email' is required when using '--mode=workload-identity'.");
            }
            if (!SA_EMAIL_PATTERN.matcher(serviceAccountEmail).matches()) {
                throw new IllegalArgumentException("Invalid service account email format. Expected format: <name>@<project>.iam.gserviceaccount.com");
            }
            if (workloadIdentityProvider == null) {
                throw new IllegalArgumentException("Option '--workload-identity-provider' is required when using '--mode=workload-identity'.");
            }
            if (!WIF_PROVIDER_PATTERN.matcher(workloadIdentityProvider).matches()) {
                throw new IllegalArgumentException("Invalid Workload Identity Provider format. Expected: projects/{PROJECT_NUMBER}/locations/global/workloadIdentityPools/{POOL}/providers/{PROVIDER}");
            }
        } else {
            if (serviceAccountEmail != null || workloadIdentityProvider != null || tokenAudience != null) {
                throw new IllegalArgumentException("Options '--service-account-email', '--workload-identity-provider', and '--token-audience' can only be used with '--mode=workload-identity'.");
            }
            if (serviceAccountKey == null) {
                throw new IllegalArgumentException("Option '--key' is required when using service account key mode.");
            }
        }
    }
}
