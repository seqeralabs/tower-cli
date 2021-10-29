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
import io.seqera.tower.JSON;

public class JsonHelper {

    private JsonHelper() {
    }

    public static String prettyJson(Object obj) throws JsonProcessingException {
        return new JSON().getContext(obj.getClass()).writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    public static <T> T parseJson(String json, Class<T> clazz) throws JsonProcessingException {
        return new JSON().getContext(clazz).readValue(json, clazz);
    }

}
