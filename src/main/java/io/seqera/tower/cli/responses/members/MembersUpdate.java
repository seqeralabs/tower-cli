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

package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;

public class MembersUpdate extends Response {

    public final String user;
    public final String organizationName;
    public final String role;

    public MembersUpdate(String user, String organizationName, String role) {
        this.user = user;
        this.organizationName = organizationName;
        this.role = role;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' updated to role '%s' in organization '%s'|@%n", user, role, organizationName));
    }
}
