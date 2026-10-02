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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import io.seqera.tower.JSON;

public class JsonHelper {

    private JsonHelper() {
    }

    public static String prettyJson(Object obj) throws JsonProcessingException {
        // SDK fields declared nullable are JsonNullable, which NON_NULL does not omit when the server sends an explicit null
        ObjectMapper mapper = new JSON().getMapper().configure(JsonNodeFeature.WRITE_NULL_PROPERTIES, false);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.valueToTree(obj));
    }

    public static <T> T parseJson(String json, Class<T> clazz) throws JsonProcessingException {
        return new JSON().getContext(clazz).readValue(json, clazz);
    }

}
