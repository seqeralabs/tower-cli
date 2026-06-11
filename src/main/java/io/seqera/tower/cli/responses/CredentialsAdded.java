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

package io.seqera.tower.cli.responses;

public class CredentialsAdded extends Response {

    public final String id;
    public final String provider;
    public final String name;
    public final String workspaceRef;
    /**
     * Generated External ID for AWS role-mode credentials when --generate-external-id is used.
     * Null for other providers or when External ID generation was not requested.
     */
    public final String externalId;
    /**
     * Server-rendered provider-side setup snippet (e.g. AWS IAM role trust policy) that the
     * user should paste at their cloud provider. Populated when a renderer is available for
     * the credential type and the installation is configured for it; otherwise null.
     */
    public final String setupSnippet;

    public CredentialsAdded(String provider, String id, String name, String workspaceRef) {
        this(provider, id, name, workspaceRef, null, null);
    }

    public CredentialsAdded(String provider, String id, String name, String workspaceRef,
                            String externalId, String setupSnippet) {
        this.provider = provider;
        this.id = id;
        this.name = name;
        this.workspaceRef = workspaceRef;
        this.externalId = externalId;
        this.setupSnippet = setupSnippet;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(ansi(String.format("%n  @|yellow New %S credentials '%s (%s)' added at %s workspace|@%n",
                provider, name, id, workspaceRef)));
        if (externalId != null && !externalId.isEmpty()) {
            out.append(ansi(String.format("%n  @|bold External ID:|@ %s%n", externalId)));
        }
        if (setupSnippet != null && !setupSnippet.isEmpty()) {
            out.append(ansi(String.format("%n  @|bold Trust policy|@ (paste this into your IAM role's trust relationship):%n%n%s%n",
                    indent(setupSnippet, "    "))));
        }
        return out.toString();
    }

    private static String indent(String text, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\\R", -1)) {
            sb.append(prefix).append(line).append('\n');
        }
        return sb.toString();
    }
}
