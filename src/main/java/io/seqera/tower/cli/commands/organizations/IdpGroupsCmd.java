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

import io.seqera.tower.cli.commands.AbstractRootCmd;
import io.seqera.tower.cli.commands.organizations.idpgroups.AddCmd;
import io.seqera.tower.cli.commands.organizations.idpgroups.DeleteCmd;
import io.seqera.tower.cli.commands.organizations.idpgroups.ListCmd;
import picocli.CommandLine.Command;

@Command(
        name = "idp-groups",
        description = "Manage the organization IdP group catalog used to link teams to IdP groups. Requires IdP claims mapping to be enabled for the organization.",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                DeleteCmd.class,
        }
)
public class IdpGroupsCmd extends AbstractRootCmd {
}
