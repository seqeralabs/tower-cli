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

package io.seqera.tower.cli.commands.organizations;

import picocli.CommandLine;

public class OrganizationsOptions {

    @CommandLine.Option(names = {"-d", "--description"}, description = "Organization description. Free-text description providing context about the organization's purpose, team, or projects.")
    public String description;

    @CommandLine.Option(names = {"-l", "--location"}, description = "Organization location. Geographic location or region where the organization is based (e.g., 'San Francisco, CA' or 'EU').")
    public String location;

    @CommandLine.Option(names = {"-w", "--website"}, description = "Organization website URL. Public website or documentation site for the organization. Must be a valid URL (e.g., https://example.com).")
    public String website;
}
