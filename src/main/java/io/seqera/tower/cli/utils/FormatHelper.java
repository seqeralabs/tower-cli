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

    public static String formatTime(OffsetDateTime value) {
        if (value == null) {
            return "never";
        }

        return value.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
