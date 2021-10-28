package io.seqera.tower.cli.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YmlHelper {

    public YmlHelper() {
    }

    public static <T> T parseYml(String yml, Class<T> clazz) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yml, clazz);
    }
}
