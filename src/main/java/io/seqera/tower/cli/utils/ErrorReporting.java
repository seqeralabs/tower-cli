package io.seqera.tower.cli.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.exceptions.ApiExceptionMessage;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;

public class ErrorReporting {

    private ErrorReporting() {
    }

    public static void errorMessage(Tower app, Exception e) {
        errorMessage(app, app.getErr(), e);
    }

    public static void errorMessage(Tower app, PrintWriter err, Exception e) {

        if (e instanceof ShowUsageException) {
            app.spec.commandLine().usage(err);
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
                    print(err, decodeMessage(ex.getResponseBody()));
            }
            return;
        }

        e.printStackTrace(err);
    }

    private static void print(PrintWriter err, String line) {
        err.println(CommandLine.Help.Ansi.AUTO.string((String.format("%n @|bold,red ERROR:|@ @|red %s|@%n", line))));
    }

    private static String decodeMessage(String body) {
        if (body == null) {
            return "";
        }

        try {
            ApiExceptionMessage message = parseJson(body, ApiExceptionMessage.class);
            return message.getMessage();
        } catch (JsonProcessingException e) {
            // On exception return as it is
        }

        return body;

    }

}
