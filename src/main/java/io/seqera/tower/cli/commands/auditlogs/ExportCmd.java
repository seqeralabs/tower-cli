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
import io.seqera.tower.cli.responses.auditlogs.AuditLogsExported;
import io.seqera.tower.model.AuditLogV2QueryAttribute;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Command(
        name = "export",
        description = "Export audit logs as CSV. Fails when too many logs match: narrow the date range. Requires a root user."
)
public class ExportCmd extends AbstractApiCmd {

    @Mixin
    public AuditLogsFilterOptions filter;

    @Option(names = {"--state"}, description = "Include the state of each target before and after the change. Requires the audit state images feature.")
    public boolean state;

    @Option(names = {"-o", "--output"}, description = "Output CSV file path. Prints the CSV to stdout when omitted.")
    public Path output;

    @Override
    protected Response exec() throws ApiException, IOException {
        // The SDK downloads into a temp file; it holds installation-wide audit data, so never leave it behind
        Path csv = adminApi().exportAuditLogsV2Csv(state ? List.of(AuditLogV2QueryAttribute.state) : null, filter.after, filter.before).toPath();
        try {
            if (output == null) {
                return new AuditLogsExported(null, Files.readString(csv));
            }
            Files.move(csv, output, StandardCopyOption.REPLACE_EXISTING);
            return new AuditLogsExported(output.toString(), null);
        } finally {
            Files.deleteIfExists(csv);
        }
    }
}
