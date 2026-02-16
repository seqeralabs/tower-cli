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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Deserializer that accepts both JSON numbers and quoted strings for Integer fields.
 * <p>
 * Workaround for the Platform API returning {@code "exit": "0"} (string) while the
 * SDK model declares the field as {@code Integer}. The SDK disables
 * {@code ALLOW_COERCION_OF_SCALARS} globally, so this deserializer is applied
 * only to the specific field via a Jackson mixin.
 *
 * FIXME: Workaround for Platform versions before 26.x. Remove once those versions are phased out (see #578).
 */
public class LenientIntegerDeserializer extends StdDeserializer<Integer> {

    public LenientIntegerDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            return text.isEmpty() ? null : Integer.valueOf(text);
        }
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        return p.getIntValue();
    }
}
