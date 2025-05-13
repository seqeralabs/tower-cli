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

package io.seqera.tower.model;

/**
 * Security keys for Azure Repos credentials.
 */
public class AzureReposSecurityKeys implements SecurityKeys {

    private String username;
    private String token;

    /**
     * Get username associated with this security keys.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username associated with this security keys.
     *
     * @param username The username
     * @return This AzureReposSecurityKeys instance
     */
    public AzureReposSecurityKeys username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get token associated with this security keys.
     *
     * @return The token
     */
    public String getToken() {
        return token;
    }

    /**
     * Set token associated with this security keys.
     *
     * @param token The token
     * @return This AzureReposSecurityKeys instance
     */
    public AzureReposSecurityKeys token(String token) {
        this.token = token;
        return this;
    }
}