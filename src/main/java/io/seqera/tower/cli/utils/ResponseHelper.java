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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.StringUtil;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.ApiExceptionMessage;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import org.glassfish.jersey.internal.guava.Sets;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import picocli.CommandLine;

import javax.ws.rs.ProcessingException;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static io.seqera.tower.cli.utils.JsonHelper.prettyJson;

public class ResponseHelper {

    private ResponseHelper() {
    }

    public static int outputFormat(PrintWriter out, Response response, OutputType outputType) throws JsonProcessingException {
        if (outputType == OutputType.json) {
            out.println(prettyJson(response.getJSON()));
        } else {
            response.toString(out);
        }
        return response.getExitCode();
    }

    public static void errorMessage(Tower app, Exception e) {
        errorMessage(app.getErr(), e);
    }

    public static void errorMessage(PrintWriter err, Exception e) {

        if (e instanceof ShowUsageException) {

            String message = e.getMessage();
            if(message != null && !message.trim().equals("")) {
                err.append(CommandLine.Help.Ansi.AUTO.string((String.format("%n@|bold,red ERROR:|@ @|red %s|@%n", message))));
            }
            ((ShowUsageException)e).getSpecs().commandLine().usage(err);

            return;
        }

        if (e instanceof TowerException) {
            print(err, e.getMessage());
            return;
        }

        if (e instanceof NoSuchFileException) {
            print(err, String.format("File not found. %s", e.getMessage()));
            return;
        }

        if (e instanceof ApiException) {
            ApiException ex = (ApiException) e;
            switch (ex.getCode()) {
                case 401:
                    print(err, "Unauthorized. Check your access token, workspace id and tower server url.");
                    break;

                case 403:
                    print(err, "Unknown. Check that the provided identifier is correct.");
                    break;

                default:
                    print(err, decodeMessage(ex));
            }
            return;
        }

        if (e instanceof ProcessingException) {
            print(err, "Connection error. Check the connection using the command 'tw info'.");
            return;
        }

        e.printStackTrace(err);
    }

    private static void print(PrintWriter err, String line) {
        err.println(CommandLine.Help.Ansi.AUTO.string((String.format("%n @|bold,red ERROR:|@ @|red %s|@%n", line))));
    }

    private static String decodeMessage(ApiException ex) {
        if (ex.getResponseBody() == null) {
            return ex.getMessage();
        }

        try {
            ApiExceptionMessage message = parseJson(ex.getResponseBody(), ApiExceptionMessage.class);
            return message.getMessage();
        } catch (JsonProcessingException e) {
            // On exception return as it is
        }

        return ex.getResponseBody();

    }

    public static <S extends Enum<?>> Integer waitStatus(PrintWriter out, boolean showProgress, S targetStatus, S[] allStates, Supplier<S> checkStatus, S... endStates ) throws InterruptedException {

        Map<S, Integer> positions = new HashMap<>();
        for (int i=0; i < allStates.length; i++) {
            positions.put(allStates[i], i);
        }

        Set<S> immutableStates = new HashSet<S>(Arrays.asList(endStates));

        int secondsToSleep = 2;
        int maxSecondsToSleep = 120;
        int targetPos = positions.get(targetStatus);
        int currentPos;
        S lastReported = null;

        if (showProgress) {
            out.print(String.format("  Waiting %s status...", targetStatus));
            out.flush();
        }

        S status;
        do {
            TimeUnit.SECONDS.sleep(secondsToSleep);
            status = checkStatus.get();
            currentPos = status == null ? positions.size() : positions.get(status);
            if (showProgress) {
                out.print('.');
                if (lastReported != status) {
                    out.print(String.format("%s", status));
                    lastReported = status;
                }
                out.flush();
            }
            if (secondsToSleep < maxSecondsToSleep) {
                secondsToSleep += 1;
            }
        } while (currentPos < targetPos && !immutableStates.contains(status));

        if (showProgress) {
            out.print(currentPos == targetPos ? "  [DONE]\n\n" : "  [ERROR]\n\n");
            out.flush();
        }

        return currentPos == targetPos ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }

}
