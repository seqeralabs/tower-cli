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

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

import java.time.OffsetDateTime;

public class AuditLogsFilterOptions {

    @Option(names = {"--after"}, description = "Show only logs on or after this ISO-8601 date-time, e.g. 2026-01-31T00:00:00Z", converter = DateTimeConverter.class)
    public OffsetDateTime after;

    @Option(names = {"--before"}, description = "Show only logs on or before this ISO-8601 date-time, e.g. 2026-01-31T23:59:59Z", converter = DateTimeConverter.class)
    public OffsetDateTime before;

    public static class DateTimeConverter implements ITypeConverter<OffsetDateTime> {
        @Override
        public OffsetDateTime convert(String value) {
            return OffsetDateTime.parse(value);
        }
    }
}
