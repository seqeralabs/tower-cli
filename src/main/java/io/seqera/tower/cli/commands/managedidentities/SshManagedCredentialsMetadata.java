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


package io.seqera.tower.cli.commands.managedidentities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.seqera.tower.model.ManagedCredentialsMetadata;

/**
 * The SDK models managed credentials metadata without the SSH 'userName' field that Platform requires
 * (it only knows the read-only discriminator), so add it here.
 */
public class SshManagedCredentialsMetadata extends ManagedCredentialsMetadata {

    @JsonProperty("userName")
    public final String userName;

    public SshManagedCredentialsMetadata(String userName) {
        this.userName = userName;
    }
}
