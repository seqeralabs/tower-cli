/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.OrganizationDbDto;

public class OrganizationsAdded extends Response {

    public final OrganizationDbDto organization;

    public OrganizationsAdded(OrganizationDbDto organization) {
        this.organization = organization;
    }

    @Override
    public Object getJSON() {
        return organization;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Organization '%s' with ID '%d' was added|@%n", organization.getName(), organization.getOrgId()));
    }
}
