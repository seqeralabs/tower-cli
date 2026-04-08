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

package io.seqera.tower.cli.computeenvs.platforms;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvAdded;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class GoogleBatchPlatformTest extends BaseCmdTest {

    private static final String CREDENTIALS_RESPONSE = "{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}";

    private void mockCredentials(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(CREDENTIALS_RESPONSE).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"gs://workdir\"},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe");
        assertOutput(format, out, new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"usePrivateAddress\":true,\"waveEnabled\":true,\"fusion2Enabled\":true}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--fusion-v2", "--wave", "--use-private-address");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithInstanceTemplates(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"headJobInstanceTemplate\":\"projects/my-project/global/instanceTemplates/head-template\",\"computeJobsInstanceTemplate\":\"projects/my-project/global/instanceTemplates/compute-template\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--head-job-template", "projects/my-project/global/instanceTemplates/head-template", "--compute-job-template", "projects/my-project/global/instanceTemplates/compute-template");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithOnlyHeadJobTemplate(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"headJobInstanceTemplate\":\"projects/my-project/global/instanceTemplates/head-template\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--head-job-template", "projects/my-project/global/instanceTemplates/head-template");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithOnlyComputeJobTemplate(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"computeJobsInstanceTemplate\":\"projects/my-project/global/instanceTemplates/compute-template\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--compute-job-template", "projects/my-project/global/instanceTemplates/compute-template");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithNetworkTags(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"network\":\"my-vpc\",\"networkTags\":[\"allow-ssh\",\"web-tier\"]}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--network", "my-vpc", "--network-tags", "allow-ssh,web-tier");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithNetworkAndSubnetwork(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"network\":\"my-vpc\",\"subnetwork\":\"my-subnet\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--network", "my-vpc", "--subnetwork", "my-subnet");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddNetworkTagsWithoutVpcFails(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--network-tags", "allow-ssh");

        assertTrue(out.stdErr.contains("Network tags require VPC configuration"), "Expected VPC required error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddNetworkTagsInvalidFormat(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--network", "my-vpc", "--network-tags", "Allow-SSH");

        assertTrue(out.stdErr.contains("Invalid network tag 'Allow-SSH'"), "Expected invalid tag error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddNetworkTagsEndsWithHyphen(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--network", "my-vpc", "--network-tags", "a-");

        assertTrue(out.stdErr.contains("Invalid network tag 'a-'"), "Expected invalid tag error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddNetworkTagsSingleDigitInvalid(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--network", "my-vpc", "--network-tags", "1");

        assertTrue(out.stdErr.contains("Invalid network tag '1'"), "Expected invalid single-char tag error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddNetworkTagsSingleLetterValid(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"network\":\"my-vpc\",\"networkTags\":[\"a\"]}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--network", "my-vpc", "--network-tags", "a");
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithHeadJobMachineType(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"machineType\":\"n2-standard-4\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--head-job-machine-type", "n2-standard-4");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithComputeJobsMachineType(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"computeJobsMachineType\":[\"n2-standard-8\",\"c2-standard-4\"]}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--compute-jobs-machine-type", "n2-standard-8,c2-standard-4");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithComputeJobsWildcardMachineType(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"computeJobsMachineType\":[\"n2-*\"]}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--compute-jobs-machine-type", "n2-*");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddHeadJobMachineTypeAndTemplateAreMutuallyExclusive(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--head-job-machine-type", "n2-standard-4",
                "--head-job-template", "projects/my-project/global/instanceTemplates/head-template");

        assertTrue(out.stdErr.contains("Head job machine type and head job instance template are mutually exclusive"), "Expected mutual exclusivity error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddComputeJobsMachineTypeAndTemplateAreMutuallyExclusive(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--compute-jobs-machine-type", "n2-standard-8",
                "--compute-job-template", "projects/my-project/global/instanceTemplates/compute-template");

        assertTrue(out.stdErr.contains("Compute jobs machine type and compute jobs instance template are mutually exclusive"), "Expected mutual exclusivity error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddHeadJobWildcardMachineTypeRejected(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--head-job-machine-type", "n2-*");

        assertTrue(out.stdErr.contains("Wildcard machine type families are not supported for the head job"), "Expected wildcard rejection error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddInvalidMachineTypeFormat(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--head-job-machine-type", "N2-Standard-4");

        assertTrue(out.stdErr.contains("Invalid machine type 'N2-Standard-4'"), "Expected invalid format error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddWithBootDiskImage(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"bootDiskImage\":\"projects/ubuntu-os-cloud/global/images/ubuntu-2404-noble-amd64-v20250112\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--boot-disk-image", "projects/ubuntu-os-cloud/global/images/ubuntu-2404-noble-amd64-v20250112");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithBootDiskImageFamily(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"bootDiskImage\":\"projects/ubuntu-os-cloud/global/images/family/ubuntu-2404-lts\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--boot-disk-image", "projects/ubuntu-os-cloud/global/images/family/ubuntu-2404-lts");
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithBootDiskImageBatchShortName(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"bootDiskImage\":\"batch-debian\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--boot-disk-image", "batch-debian");
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithInvalidBootDiskImage(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--boot-disk-image", "invalid/image/path");

        assertTrue(out.stdErr.contains("Invalid boot disk image format"), "Expected invalid boot disk image error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAddWithFusionSnapshots(MockServerClient mock) throws IOException {

        mock.reset();

        mockCredentials(mock);

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":true,\"fusionSnapshots\":true,\"waveEnabled\":true}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--fusion-v2", "--wave", "--fusion-snapshots");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddFusionSnapshotsRequiresFusionV2(MockServerClient mock) {

        mock.reset();

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe",
                "--wave", "--fusion-snapshots");

        assertTrue(out.stdErr.contains("Fusion Snapshots requires Fusion v2"), "Expected fusion v2 required error, got: " + out.stdErr);
        assertEquals(1, out.exitCode);
    }

}
