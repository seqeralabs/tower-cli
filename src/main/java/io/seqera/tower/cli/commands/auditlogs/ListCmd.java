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


package io.seqera.tower.cli.commands.auditlogs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.auditlogs.AuditLogsList;
import io.seqera.tower.model.ListAuditLogV2Response;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;


@Command(
        name = "list",
        description = "List audit logs, newest first. Requires a root user."
)
public class ListCmd extends AbstractApiCmd {

    @Mixin
    public AuditLogsFilterOptions filter;

    @Option(names = {"--max"}, description = "Maximum number of logs per page")
    public Integer max;

    @Option(names = {"--page-token"}, description = "Token of the page to show, as printed by a previous list with the same filters")
    public String pageToken;

    @Override
    protected Response exec() throws ApiException {
        ListAuditLogV2Response response = adminApi().listAuditLogsV2(null, pageToken, max, filter.after, filter.before);
        return new AuditLogsList(response.getAuditLogs(), response.getNextPageToken());
    }
}
