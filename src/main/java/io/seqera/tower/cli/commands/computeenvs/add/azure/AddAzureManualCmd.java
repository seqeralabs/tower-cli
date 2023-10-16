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

package io.seqera.tower.cli.commands.computeenvs.add.azure;

import io.seqera.tower.cli.commands.computeenvs.add.AbstractAddCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.AzBatchManualPlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "manual",
        description = "Add new Azure Batch compute environment using an existing environment."
)
public class AddAzureManualCmd extends AbstractAddCmd {

    @Mixin
    public AzBatchManualPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
