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

package io.seqera.tower.cli.commands;


import io.seqera.tower.cli.commands.datasets.AddCmd;
import io.seqera.tower.cli.commands.datasets.DeleteCmd;
import io.seqera.tower.cli.commands.datasets.DownloadCmd;
import io.seqera.tower.cli.commands.datasets.ListCmd;
import io.seqera.tower.cli.commands.datasets.UpdateCmd;
import io.seqera.tower.cli.commands.datasets.UrlCmd;
import io.seqera.tower.cli.commands.datasets.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "datasets",
        description = "Manage datasets.",
        subcommands = {
                AddCmd.class,
                DeleteCmd.class,
                DownloadCmd.class,
                ListCmd.class,
                ViewCmd.class,
                UpdateCmd.class,
                UrlCmd.class,
        }
)
public class DatasetsCmd extends AbstractRootCmd{
}
