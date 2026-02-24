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

import io.seqera.tower.cli.commands.participants.AddCmd;
import io.seqera.tower.cli.commands.participants.DeleteCmd;
import io.seqera.tower.cli.commands.participants.LeaveCmd;
import io.seqera.tower.cli.commands.participants.ListCmd;
import io.seqera.tower.cli.commands.participants.UpdateCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "participants",
        description = "Manage workspace participants",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                UpdateCmd.class,
                DeleteCmd.class,
                LeaveCmd.class,
        }
)
public class ParticipantsCmd extends AbstractRootCmd {
}
