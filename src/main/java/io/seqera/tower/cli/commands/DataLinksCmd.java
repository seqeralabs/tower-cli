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

import io.seqera.tower.cli.commands.data.links.AddCmd;
import io.seqera.tower.cli.commands.data.links.DeleteCmd;
import io.seqera.tower.cli.commands.data.links.DownloadCmd;
import io.seqera.tower.cli.commands.data.links.ListCmd;
import io.seqera.tower.cli.commands.data.links.UpdateCmd;
import io.seqera.tower.cli.commands.data.links.BrowseCmd;
import io.seqera.tower.cli.commands.data.links.UploadCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "data-links",
        description = "Manage data links",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                DeleteCmd.class,
                UpdateCmd.class,
                BrowseCmd.class,
                DownloadCmd.class,
                UploadCmd.class
        }
)
public class DataLinksCmd extends AbstractRootCmd {
}
