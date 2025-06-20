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

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.seqera.tower.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.ResponseHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import picocli.CommandLine;
import picocli.CommandLine.ExitCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static io.seqera.tower.cli.Tower.buildCommandLine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockServerExtension.class)
public abstract class BaseCmdTest {

    @TempDir
    Path tempDir;

    protected byte[] loadResource(String name) {
        return loadResource(name, "json");
    }

    protected byte[] loadResource(String name, String ext) {
        try (InputStream stream = this.getClass().getResourceAsStream("/runcmd/" + name + "." + ext)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    protected String tempFile(String content, String prefix, String suffix) throws IOException {
        Path file = Files.createTempFile(tempDir, prefix, suffix);
        Files.writeString(file, content);
        return file.toAbsolutePath().toString();
    }

    protected Path tempDir() {
        return tempDir;
    }

    protected String url(MockServerClient mock) {
        return String.format("http://localhost:%d", mock.getPort());
    }

    protected String token() {
        return "fake_auth_token";
    }

    protected ExecOut exec(MockServerClient mock, String... args) {
        return exec(OutputType.console, mock, args);
    }

    protected ExecOut exec(OutputType format, MockServerClient mock, String... args) {

        // Run binary command line
        if (System.getenv().containsKey("TOWER_CLI")) {
            return execBinary(format, mock, System.getenv("TOWER_CLI"), args);
        }

        // Run java version
        StringWriter stdOut = new StringWriter();
        StringWriter stdErr = new StringWriter();
        CommandLine cmd = buildCommandLine();
        cmd.setOut(new PrintWriter(stdOut));
        cmd.setErr(new PrintWriter(stdErr));

        int exitCode = cmd.execute(buildArgs(format, mock, args));

        return new ExecOut()
                .app(cmd.getCommand())
                .stdOut(StringUtils.chop(stdOut.toString()))
                .stdErr(StringUtils.chop(stdErr.toString()))
                .exitCode(exitCode);
    }

    private String[] buildArgs(OutputType format, MockServerClient mock, String... args) {
        String[] result = ArrayUtils.insert(0, args, "--insecure", String.format("--url=%s", url(mock)), String.format("--access-token=%s", token()));
        if (format != OutputType.console)  {
            return ArrayUtils.insert(0, result, String.format("--output=%s", format));
        }
        return result;
    }

    private ExecOut execBinary(OutputType format, MockServerClient mock, String command, String... args) {

        try {
            StringWriter stdOut = new StringWriter();
            StringWriter stdErr = new StringWriter();

            PrintWriter outWriter = new PrintWriter(stdOut);
            PrintWriter errWriter = new PrintWriter(stdErr);

            ProcessBuilder builder = new ProcessBuilder();
            builder.command(ArrayUtils.insert(0, buildArgs(format, mock, args), command));
            Process process = builder.start();

            StreamGobbler consumeOut = new StreamGobbler(process.getInputStream(), outWriter::println);
            StreamGobbler consumeErr = new StreamGobbler(process.getErrorStream(), errWriter::println);
            Future<?> taskOut = Executors.newSingleThreadExecutor().submit(consumeOut);
            Future<?> taskErr = Executors.newSingleThreadExecutor().submit(consumeErr);
            int exitCode = process.waitFor();
            try {
                taskErr.get(5, TimeUnit.SECONDS);
                taskOut.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                // Ignore this
            }

            if (exitCode == 255) {
                exitCode = -1;
            }

            outWriter.close();
            errWriter.close();

            return new ExecOut()
                    .stdOut(StringUtils.chop(stdOut.toString()))
                    .stdErr(StringUtils.chop(stdErr.toString()))
                    .exitCode(exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ExecOut()
                    .stdOut("")
                    .stdErr(e.getMessage())
                    .exitCode(-1);
        }
    }

    protected String errorMessage(Tower app, Exception e) {
        StringWriter out = new StringWriter();
        ResponseHelper.errorMessage(new PrintWriter(out), e);
        return StringUtils.chop(out.toString());
    }

    protected void assertOutput(OutputType format, ExecOut realOutput, Response expectedResponse) {
        StringWriter writer = new StringWriter();
        try {
            int expectedExitCode = ResponseHelper.outputFormat(new PrintWriter(writer), expectedResponse, format);
            String expectedOut = StringUtils.chop(writer.toString());

            // Check empty stderr
            assertEquals("", expectedExitCode == ExitCode.OK ? realOutput.stdErr : realOutput.stdOut);

            // Check expected stdout
            assertEquals(expectedOut, expectedExitCode == ExitCode.OK ? realOutput.stdOut : realOutput.stdErr);

            // Check expected exit code
            assertEquals(expectedExitCode, realOutput.exitCode);

        } catch (JsonProcessingException e) {
            fail(e);
        }
    }

    protected String baseUserUrl(MockServerClient mock, String userName) {
        return String.format("%s/user/%s", url(mock), userName);
    }

    protected String baseOrgUrl(MockServerClient mock, String orgName) {
        return String.format("%s/orgs/%s", url(mock), orgName);
    }

    protected String baseWorkspaceUrl(MockServerClient mock, String orgName, String workspaceName) {
        return String.format("%s/orgs/%s/workspaces/%s", url(mock), orgName, workspaceName);
    }

    public static class ExecOut {
        public Tower app;
        public String stdOut;
        public String stdErr;
        public int exitCode;

        public ExecOut stdOut(String value) {
            this.stdOut = value;
            return this;
        }

        public ExecOut stdErr(String value) {
            this.stdErr = value;
            return this;
        }

        public ExecOut exitCode(int value) {
            this.exitCode = value;
            return this;
        }

        public ExecOut app(Tower app) {
            this.app = app;
            return this;
        }
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }


}
