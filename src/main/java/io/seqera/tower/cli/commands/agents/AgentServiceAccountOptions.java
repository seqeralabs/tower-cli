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

package io.seqera.tower.cli.commands.agents;

import picocli.CommandLine.Option;

public class AgentServiceAccountOptions {

    @Option(names = {"--service-account-id"}, description = "Service account numeric identifier, as shown by 'tw service-accounts list'. The agent runs with the permissions of this service account in the workspace.")
    public Long id;

    @Option(names = {"--service-account"}, description = "Service account name. The agent runs with the permissions of this service account in the workspace. Looking up a service account by name requires the organization owner role.")
    public String name;
}
