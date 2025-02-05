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

package io.seqera.tower.cli.utils;

import io.seqera.tower.model.*;
import picocli.CommandLine;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class FormatHelper {

    public static String formatDate(OffsetDateTime date) {
        if (date == null) {
            return "";
        }

        return date.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public static String formatDurationMillis(Long duration) {
        if (duration == null) {
            return "";
        }

        Duration d = Duration.ofMillis(duration);
        long days = d.toDaysPart();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();

        String result = "";
        if (days != 0) {
            result = String.format("%s%dd", result, days);
        }
        if (hours != 0) {
            result = String.format("%s%dh", result, hours);
        }
        if (minutes != 0) {
            result = String.format("%s%dm", result, minutes);
        }
        if (seconds != 0) {
            result = String.format("%s%ds", result, seconds);
        }

        return result;
    }

    public static String formatDurationMillis(Number value) {
        if (value == null) {
            return "";
        }

        return formatDurationMillis(value.longValue());
    }

    public static String formatTime(OffsetDateTime value) {
        if (value == null) {
            return "never";
        }

        return value.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public static String formatBits(Long value) {
        if (value == null) {
            return "";
        }

        Double amount = null;
        String metric;

        amount = value / 1024 / 1024 / 1024D;
        metric = "GB";

        if (amount <= 1) {
            amount = value / 1024 / 1024D;
            metric = "MB";
        }

        if (amount <= 1) {
            amount = value / 1024D;
            metric = "KB";
        }

        if (amount <= 1) {
            amount = value * 1D;
            metric = "B";
        }

        return String.format("%.0f%s", amount, metric);
    }

    public static String formatBits(Number value) {
        if (value == null) {
            return "";
        }

        return formatBits(value.longValue());
    }

    public static String formatCost(Double value) {
        if (value == null) {
            return "";
        }

        return String.format("$%f", value);
    }

    public static String formatPercentage(Double value) {
        if (value == null) {
            return "";
        }

        return String.format("%.0f%%", value);
    }

    public static String formatPercentage(Number value) {
        if (value == null) {
            return "";
        }

        return formatPercentage(value.doubleValue());
    }

    public static String formatDecimal(Double value) {
        if (value == null) {
            return "";
        }

        return String.format("%.2f", value);
    }


    private static final boolean ANSI_ENABLED = CommandLine.Help.Ansi.AUTO.enabled();

    public static String formatWorkflowId(String workflowId, String baseWorkspaceUrl) {
        return formatLink(workflowId, String.format("%s/watch/%s", baseWorkspaceUrl, workflowId));
    }

    public static String formatActionId(String actionId, String baseWorkspaceUrl) {
        return formatLink(actionId, String.format("%s/actions/%s", baseWorkspaceUrl, actionId));
    }

    public static String formatComputeEnvId(String ceId, String baseWorkspaceUrl) {
        return formatLink(ceId, String.format("%s/compute-envs/%s", baseWorkspaceUrl, ceId));
    }

    public static String formatCredentialsId(String credentialsId, String baseWorkspaceUrl) {
        return formatLink(credentialsId, String.format("%s/credentials/%s/edit", baseWorkspaceUrl, credentialsId));
    }

    public static String formatTeamId(Long teamId, String baseOrgUrl) {
        return formatLink(Long.toString(teamId), String.format("%s/teams/%s/members", baseOrgUrl, teamId));
    }

    public static String formatWorkspaceId(Long workspaceId, String serverUrl, String orgName, String workspaceName) {
        return formatLink(Long.toString(workspaceId), String.format("%s/orgs/%s/workspaces/%s/launchpad", serverUrl, orgName, workspaceName));
    }

    public static String formatOrgId(Long orgId, String serverUrl, String orgName) {
        return formatLink(Long.toString(orgId), String.format("%s/orgs/%s/workspaces", serverUrl, orgName));
    }

    public static String formatPipelineId(Long pipelineId, String baseWorkspaceUrl) {
        return formatLink(Long.toString(pipelineId), String.format("%s/launchpad/%s/edit", baseWorkspaceUrl, pipelineId));
    }

    private static String formatLink(String title, String link) {
        return ANSI_ENABLED ? "\u001b]8;;" + link + "\u001b\\" + title + "\u001b]8;;\u001b\\" : title;
    }

    public static String formatWorkflowStatus(WorkflowStatus status) {

        if (status == null) {
            return "NA";
        }

        if (WorkflowStatus.SUBMITTED.equals(status)) {
            return ansi("@|fg(214) SUBMITTED|@");
        }

        if (WorkflowStatus.RUNNING.equals(status)) {
            return ansi("@|fg(blue) RUNNING|@");
        }

        if (WorkflowStatus.SUCCEEDED.equals(status)) {
            return ansi("@|fg(green) SUCCEEDED|@");
        }

        if (WorkflowStatus.FAILED.equals(status)) {
            return ansi("@|fg(red) FAILED|@");
        }

        if (WorkflowStatus.CANCELLED.equals(status)) {
            return ansi("@|fg(white) CANCELLED|@");
        }

        return status.toString();
    }

    public static String formatActionStatus(ActionStatus status) {
        if (status == null) {
            return "NA";
        }

        switch (status) {
            case ACTIVE:
                return ansi("@|fg(green) ACTIVE|@");
            case ERROR:
                return ansi("@|fg(red) ERROR|@");
            case PAUSED:
                return ansi("@|fg(white) PAUSED|@");
            case CREATING:
                return ansi("@|fg(214) CREATING|@");
            default:
                return status.toString();
        }
    }

    public static String formatComputeEnvStatus(ComputeEnvStatus status) {
        if (status == null) {
            return "NA";
        }

        switch (status) {
            case CREATING:
                return ansi("@|fg(214) CREATING|@");
            case ERRORED:
                return ansi("@|fg(red) ERRORED|@");
            case INVALID:
                return ansi("@|fg(red) INVALID|@");
            case AVAILABLE:
                return ansi("@|fg(green) AVAILABLE|@");
            default:
                return status.toString();
        }
    }

    public static String formatStudioStatus(DataStudioStatus status) {
        if (status == null) {
            return "NA";
        }

        switch (status) {
            case STARTING:
                return ansi("@|fg(214) STARTING|@");
            case RUNNING:
                return ansi("@|fg(green) RUNNING|@");
            case STOPPING:
                return ansi("@|fg(214) STOPPING|@");
            case STOPPED:
                return ansi("@|fg(214) STOPPED|@");
            case ERRORED:
                return ansi("@|fg(red) ERRORED|@");
            case BUILDING:
                return ansi("@|fg(214) BUILDING|@");
            case BUILDFAILED:
                return ansi("@|fg(red) BUILDFAILED|@");
            default:
                return status.toString();
        }
    }


    public static String formatParticipantType(ParticipantType participantType) {
        if (participantType == null) {
            return "NA";
        }

        switch (participantType) {
            case TEAM:
                return ansi("@|fg(blue) TEAM|@");
            case MEMBER:
                return ansi("@|fg(green) MEMBER|@");
            case COLLABORATOR:
                return ansi("@|fg(214) COLLABORATOR|@");
            default:
                return participantType.toString();
        }

    }

    public static String formatOrgRole(OrgRole role) {
        if (role == null) {
            return "NA";
        }

        switch (role) {
            case COLLABORATOR:
                return ansi("@|fg(214) COLLABORATOR|@");
            case MEMBER:
                return ansi("@|fg(green) MEMBER|@");
            case OWNER:
                return ansi("@|fg(magenta) OWNER|@");
            default:
                return role.toString();
        }
    }

    public static String ansi(String value) {
        return CommandLine.Help.Ansi.AUTO.string(value);
    }

    public static String formatLabels(java.util.List<io.seqera.tower.model.LabelDbDto> res) {
        if (res == null || res.isEmpty()) {
            return "";
        }

        return res.stream().map(label -> {
            String str = label.getName();
            if (label.getValue() != null && !label.getValue().isEmpty()) {
                str += "=" + label.getValue();
            }
            return str;
        })
        .collect(Collectors.joining(","));
    }

    public static String formatDescription(String description) {
        return formatDescription(description, 2048);
    }

    public static String formatDescription(String description, int maxLength) {
        if (description == null) {
            return "NA";
        }

        // remove line breaks
        var text = description.trim().replace("\n", " ").replace("\r", " ");
        // cap the description length if too long
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

}
