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
