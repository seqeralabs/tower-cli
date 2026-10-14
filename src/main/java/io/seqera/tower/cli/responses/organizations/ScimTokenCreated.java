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

package io.seqera.tower.cli.responses.organizations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CreateScimTokenResponse;

public class ScimTokenCreated extends Response {

    public final String organizationName;
    public final CreateScimTokenResponse scimToken;

    @JsonIgnore
    private final boolean rotated;

    public ScimTokenCreated(String organizationName, CreateScimTokenResponse scimToken, boolean rotated) {
        this.organizationName = organizationName;
        this.scimToken = scimToken;
        this.rotated = rotated;
    }

    @Override
    public Object getJSON() {
        return scimToken;
    }

    @Override
    public String toString() {
        return rotated
                ? ansi(String.format("%n  @|yellow SCIM token rotated for %s organization. Copy it now, it won't be shown again:|@%n%n    %s%n%n  SCIM endpoint URL: %s%n", organizationName, scimToken.getToken(), scimToken.getEndpointUrl()))
                : ansi(String.format("%n  @|yellow SCIM token generated for %s organization. Copy it now, it won't be shown again:|@%n%n    %s%n%n  SCIM endpoint URL: %s%n", organizationName, scimToken.getToken(), scimToken.getEndpointUrl()));
    }
}
