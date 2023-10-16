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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.primary.GetCmd;
import io.seqera.tower.cli.commands.computeenvs.primary.SetCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "primary",
        description = "Sets or gets a primary compute environment within current workspace.",
        subcommands = {
                GetCmd.class,
                SetCmd.class,
        }
)
public class PrimaryCmd extends AbstractComputeEnvCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
