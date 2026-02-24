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

package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.workspaces.AddCmd;
import io.seqera.tower.cli.commands.workspaces.DeleteCmd;
import io.seqera.tower.cli.commands.workspaces.LeaveCmd;
import io.seqera.tower.cli.commands.workspaces.ListCmd;
import io.seqera.tower.cli.commands.workspaces.UpdateCmd;
import io.seqera.tower.cli.commands.workspaces.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "workspaces",
        description = "Manage workspaces",
        subcommands = {
                ListCmd.class,
                DeleteCmd.class,
                AddCmd.class,
                UpdateCmd.class,
                ViewCmd.class,
                LeaveCmd.class,
        }
)
public class WorkspacesCmd extends AbstractRootCmd {
}
