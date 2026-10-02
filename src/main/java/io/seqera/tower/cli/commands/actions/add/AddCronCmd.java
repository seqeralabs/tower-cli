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

package io.seqera.tower.cli.commands.actions.add;

import io.seqera.tower.model.ActionSource;
import io.seqera.tower.model.CreateActionRequest;
import io.seqera.tower.model.CronActionRequest;
import picocli.CommandLine;

@CommandLine.Command(
        name = "cron",
        description = "Add a pipeline action triggered on a recurring schedule"
)
public class AddCronCmd extends AbstractAddCmd {

    @CommandLine.Option(names = {"--cron-expression"}, description = "Schedule as a standard 5-field cron expression (e.g., '0 2 * * *').", required = true)
    public String cronExpression;

    @CommandLine.Option(names = {"--timezone"}, description = "IANA timezone the schedule runs in (e.g., Europe/London). Default: UTC.")
    public String timezone;

    @Override
    protected ActionSource getSource() {
        return ActionSource.cron;
    }

    @Override
    protected void configureTrigger(CreateActionRequest request) {
        request.setCron(new CronActionRequest().expression(cronExpression).timezone(timezone));
    }
}
