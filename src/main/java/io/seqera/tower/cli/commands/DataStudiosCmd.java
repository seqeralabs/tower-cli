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

import io.seqera.tower.cli.commands.datastudios.AddCmd;
import io.seqera.tower.cli.commands.datastudios.ListCmd;
import io.seqera.tower.cli.commands.datastudios.StartAsNewCmd;
import io.seqera.tower.cli.commands.datastudios.StartCmd;
import io.seqera.tower.cli.commands.datastudios.TemplatesCmd;
import io.seqera.tower.cli.commands.datastudios.StopCmd;
import io.seqera.tower.cli.commands.datastudios.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "studios",
        description = "Manage data studios.",
        subcommands = {
                ViewCmd.class,
                ListCmd.class,
                StartCmd.class,
                AddCmd.class,
                TemplatesCmd.class,
                StartAsNewCmd.class,
                StopCmd.class
        }
)
public class DataStudiosCmd extends AbstractRootCmd {
}
