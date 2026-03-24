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


import io.seqera.tower.cli.commands.teams.AddCmd;
import io.seqera.tower.cli.commands.teams.DeleteCmd;
import io.seqera.tower.cli.commands.teams.ListCmd;
import io.seqera.tower.cli.commands.teams.MembersCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "teams",
        description = "Manage teams",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                DeleteCmd.class,
                MembersCmd.class,
        }
)
public class TeamsCmd extends AbstractRootCmd {
}
