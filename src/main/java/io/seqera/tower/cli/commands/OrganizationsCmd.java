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

package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.organizations.AddCmd;
import io.seqera.tower.cli.commands.organizations.DeleteCmd;
import io.seqera.tower.cli.commands.organizations.ListCmd;
import io.seqera.tower.cli.commands.organizations.UpdateCmd;
import io.seqera.tower.cli.commands.organizations.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "organizations",
        description = "Manage organizations",
        subcommands = {
                ListCmd.class,
                DeleteCmd.class,
                AddCmd.class,
                UpdateCmd.class,
                ViewCmd.class,
        }
)
public class OrganizationsCmd extends AbstractRootCmd {
}
