/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.utils;

import picocli.CommandLine;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class FormatHelper {

    public static String formatDate(OffsetDateTime date) {
        return date.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public static String formatDurationMillis(Long duration) {
        Duration d = Duration.ofMillis(duration);
        long days = d.toDaysPart();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();

        String result = "";
        if (seconds != 0) {
            result = String.format("%s%ds ", result, seconds);
        }
        if (minutes != 0) {
            result = String.format("%s%dm ", result, minutes);
        }
        if (hours != 0) {
            result = String.format("%s%dh ", result, hours);
        }
        if (days != 0) {
            result = String.format("%s%dd ", result, days);
        }

        return result;
    }

    private static final boolean ANSI_ENABLED = CommandLine.Help.Ansi.AUTO.enabled();
    public static String formatWorkflowId(String workflowId, String workflowWatchUrlPrefix) {
        if (ANSI_ENABLED) {
            String link = String.format("%s%s", workflowWatchUrlPrefix, workflowId);
            return "\u001b]8;;" + link + "\u001b\\" + workflowId + "\u001b]8;;\u001b\\";
        }
        return workflowId;
    }

    public static String formatWorkflowStatus(String status) {

        if ("SUCCEEDED".equals(status)) {
            return ansi("@|fg(green) SUCCEEDED|@");
        }

        if ("FAILED".equals(status)) {
            return ansi("@|fg(red) FAILED|@");
        }

        if ("CANCELLED".equals(status)) {
            return ansi("@|fg(white) CANCELLED|@");
        }

        return status;
    }

    private static String ansi(String value) {
        return CommandLine.Help.Ansi.AUTO.string(value);
    }
}
