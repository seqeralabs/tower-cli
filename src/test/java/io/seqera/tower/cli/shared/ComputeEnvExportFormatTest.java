/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ForgeConfig;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputeEnvExportFormatTest {

    @Test
    void testFormatDeserialization() throws JsonProcessingException {
        String json = "{\n" +
                "  \"region\" : \"eu-west-2\",\n" +
                "  \"executionRole\" : \"arn:aws:iam::<redacted>:role/TowerForge-<redacted>-ExecutionRole\",\n" +
                "  \"headJobRole\" : \"arn:aws:iam::<redacted>:role/TowerForge-<redacted>-FargateRole\",\n" +
                "  \"workDir\" : \"s3://jaime-testing\",\n" +
                "  \"waveEnabled\" : true,\n" +
                "  \"fusion2Enabled\" : true,\n" +
                "  \"nvnmeStorageEnabled\" : false,\n" +
                "  \"forge\" : {\n" +
                "    \"type\" : \"SPOT\",\n" +
                "    \"minCpus\" : 0,\n" +
                "    \"maxCpus\" : 2,\n" +
                "    \"gpuEnabled\" : false,\n" +
                "    \"disposeOnDeletion\" : true,\n" +
                "    \"efsCreate\" : false,\n" +
                "    \"fargateHeadEnabled\" : true,\n" +
                "    \"arm64Enabled\" : false\n" +
                "  },\n" +
                "  \"discriminator\" : \"aws-batch\",\n" +
                "  \"labels\" : [ {\n" +
                "    \"id\" : 130065807872087,\n" +
                "    \"name\" : \"owner\",\n" +
                "    \"value\" : \"jaime\",\n" +
                "    \"resource\" : true,\n" +
                "    \"isDefault\" : false\n" +
                "  } ]\n" +
                "}\n";

        ComputeEnvExportFormat ce = ComputeEnvExportFormat.deserialize(json);

        assertNotNull(ce.getConfig());
        assertNotNull(ce.getLabels());

        assertTrue(ce.getLabels().size() == 1);
        assertEquals("aws-batch", ce.getConfig().getDiscriminator());
    }

    @Test
    void testFormatSerialization() throws JsonProcessingException {

        AwsBatchConfig cfg = parseJson(" {\"discriminator\": \"aws-batch\"}", AwsBatchConfig.class)
                .region("eu-west-2")
                .cliPath("/home/ec2-user/miniconda/bin/aws")
                .workDir("s3://jaime-testing")
                .volumes(Collections.emptyList())
                .computeQueue("TowerForge-<redacted>-work")
                .headQueue("TowerForge-<redacted>-head")
                .forge(
                        new ForgeConfig()
                                .type(ForgeConfig.TypeEnum.SPOT)
                                .minCpus(0)
                                .maxCpus(123)
                                .gpuEnabled(false)
                                .ebsAutoScale(true)
                                .disposeOnDeletion(true)
                                .fusionEnabled(true)
                ).forgedResources(
                        List.of(
                                Map.of("IamRole", "arn:aws:iam::<redacted>:role/TowerForge-<redacted>-ServiceRole"),
                                Map.of("IamRole", "arn:aws:iam::<redacted>:role/TowerForge-<redacted>-FleetRole")
                        )
                );

        ComputeEnvExportFormat fmt = new ComputeEnvExportFormat(cfg, Collections.emptyList());
        String json = ComputeEnvExportFormat.serialize(fmt);
        ComputeEnvExportFormat newFmt = ComputeEnvExportFormat.deserialize(json);

        assertEquals(fmt.getConfig(), newFmt.getConfig());
        assertEquals(fmt.getLabels(), newFmt.getLabels());
    }

}