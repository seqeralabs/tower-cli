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

package io.seqera.tower.cli.utils;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import io.seqera.tower.JSON;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.AwsSecurityKeys;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.GitHubSecurityKeys;
import io.seqera.tower.model.SecurityKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ForwardCompatibility}: values added to the API after the CLI
 * was released must not make the response unreadable.
 */
class ForwardCompatibilityTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = ForwardCompatibility.configure(new JSON().getMapper());
    }

    @Test
    void testUnknownDiscriminatorIsReadAsBaseClass() throws IOException {
        // 'added-later' stands for a credentials type that the SDK of this CLI version does not know:
        // the keys are read as a plain SecurityKeys, keeping the discriminator and dropping 'someField'
        String json = """
                {
                  "id": "1",
                  "name": "new",
                  "provider": "aws",
                  "keys": {
                    "discriminator": "added-later",
                    "someField": "x"
                  }
                }
                """;
        Credentials credentials = mapper.readValue(json, Credentials.class);
        assertEquals(SecurityKeys.class, credentials.getKeys().getClass());
        assertEquals("added-later", credentials.getKeys().getDiscriminator());
        assertFalse(mapper.writeValueAsString(credentials.getKeys()).contains("someField"));
        assertEquals("new", credentials.getName());
    }

    @Test
    void testUnknownComputeConfigDiscriminatorIsReadAsBaseClass() throws IOException {
        // Same as above for another polymorphic model: an unknown compute platform is read as a plain
        // ComputeConfig, keeping the fields declared by the base class ('workDir')
        String json = """
                {
                  "discriminator": "added-later-platform",
                  "workDir": "s3://bucket",
                  "someField": 1
                }
                """;
        ComputeConfig config = mapper.readValue(json, ComputeConfig.class);
        assertEquals(ComputeConfig.class, config.getClass());
        assertEquals("added-later-platform", config.getDiscriminator());
        assertEquals("s3://bucket", config.getWorkDir());
        assertFalse(mapper.writeValueAsString(config).contains("someField"));
    }

    @Test
    void testSubtypeNotExtendingBaseClassIsReadAsBaseClass() throws IOException {
        // BaseKeys lists MyProviderKeys as its subtype for 'my_provider', but the class does not extend
        // BaseKeys: it is ignored, so the object is read as an unknown discriminator
        BaseKeys keys = mapper.readValue("""
                {"discriminator": "my_provider", "token": "x"}
                """, BaseKeys.class);
        assertEquals(BaseKeys.class, keys.getClass());
        assertEquals("my_provider", keys.discriminator);

        // The subtypes that do extend BaseKeys are not affected
        BaseKeys valid = mapper.readValue("""
                {"discriminator": "valid", "token": "x"}
                """, BaseKeys.class);
        assertEquals("x", assertInstanceOf(ValidKeys.class, valid).token);
    }

    @Test
    void testKnownDiscriminatorsAreStillResolved() throws IOException {
        // The types known by the SDK must keep being read as their specific class, with their own fields
        Credentials aws = mapper.readValue("""
                {"keys": {"discriminator": "aws", "accessKey": "key"}}
                """, Credentials.class);
        assertEquals("key", assertInstanceOf(AwsSecurityKeys.class, aws.getKeys()).getAccessKey());

        Credentials github = mapper.readValue("""
                {"keys": {"discriminator": "github", "username": "user"}}
                """, Credentials.class);
        assertEquals("user", assertInstanceOf(GitHubSecurityKeys.class, github.getKeys()).getUsername());

        ComputeConfig config = mapper.readValue("""
                {"discriminator": "aws-batch", "region": "eu-west-1"}
                """, ComputeConfig.class);
        assertEquals("eu-west-1", assertInstanceOf(AwsBatchConfig.class, config).getRegion());
    }

    @Test
    void testUnknownEnumValueIsReadAsNull() throws IOException {
        // 'added-later' is not a value of Credentials.ProviderEnum: the field is read as null and the
        // rest of the object is still read
        Credentials credentials = mapper.readValue("""
                {"name": "new", "provider": "added-later"}
                """, Credentials.class);
        assertNull(credentials.getProvider());
        assertEquals("new", credentials.getName());
    }

    @Test
    void testUnknownEnumValueIsFormattedAsUnknown() throws IOException {
        // The null left by an unknown enum value is printed as 'UNKNOWN' instead of throwing a NullPointerException
        Credentials credentials = mapper.readValue("""
                {"provider": "added-later"}
                """, Credentials.class);
        assertEquals("UNKNOWN", FormatHelper.formatEnum(credentials.getProvider()));
        assertEquals("aws", FormatHelper.formatEnum(Credentials.ProviderEnum.AWS));
    }

    @Test
    void testMapperWithoutItFails() {
        // Guards the tests above: the SDK mapper fails on both cases unless ForwardCompatibility is applied
        ObjectMapper strict = new JSON().getMapper();
        assertThrows(InvalidTypeIdException.class, () -> strict.readValue("""
                {"keys": {"discriminator": "added-later"}}
                """, Credentials.class));
        assertThrows(InvalidTypeIdException.class, () -> strict.readValue("""
                {"discriminator": "my_provider"}
                """, BaseKeys.class));
    }

    /**
     * Polymorphic model declared like the SDK ones, with a listed subtype that does not extend it.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "discriminator", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ValidKeys.class, name = "valid"),
            @JsonSubTypes.Type(value = MyProviderKeys.class, name = "my_provider")
    })
    static class BaseKeys {
        public String discriminator;
    }

    static class ValidKeys extends BaseKeys {
        public String token;
    }

    static class MyProviderKeys {
        public String discriminator;
        public String token;
    }
}
