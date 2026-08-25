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

import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.commands.pipelines.NullableLaunchOptions;
import io.seqera.tower.model.WorkflowLaunchRequest;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Option;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the one piece of NullableLaunchOptions that has to be kept in step by hand: an option declared
 * there but missing from applyNullableOptions would parse fine and then do nothing at all.
 */
class NullableLaunchOptionsTest {

    private static final String JSON_NULLABLE = "org.openapitools.jackson.nullable.JsonNullable";

    @Test
    void everyDeclaredOptionIsApplied() throws ReflectiveOperationException {
        Set<String> unapplied = new LinkedHashSet<>();

        for (Field option : optionFields()) {
            NullableLaunchOptions opts = new NullableLaunchOptions();
            option.set(opts, sampleValueFor(option));

            WorkflowLaunchRequest request = new WorkflowLaunchRequest();
            opts.applyNullableOptions(request);

            if (readOption(request, option.getName()) == null) {
                unapplied.add(option.getName());
            }
        }

        assertTrue(unapplied.isEmpty(), String.format(
                "%s declares options that applyNullableOptions never copies to the launch request, so they "
                        + "would be silently ignored: %s", NullableLaunchOptions.class.getSimpleName(), unapplied));
    }

    @Test
    void everyDeclaredOptionMapsToANullableRequestField() {
        Set<String> nullableRequestFields = Arrays.stream(WorkflowLaunchRequest.class.getDeclaredFields())
                .filter(f -> JSON_NULLABLE.equals(f.getType().getName()))
                .map(Field::getName)
                .collect(Collectors.toSet());

        Set<String> unknown = optionFields().stream()
                .map(Field::getName)
                .filter(name -> !nullableRequestFields.contains(name))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertTrue(unknown.isEmpty(), String.format(
                "These options are not backed by a JsonNullable field of WorkflowLaunchRequest, so they do "
                        + "not belong here: %s", unknown));
    }

    @Test
    void launchOptionsInheritsThem() {
        assertNotNull(optionFields());
        assertTrue(NullableLaunchOptions.class.isAssignableFrom(LaunchOptions.class),
                "LaunchOptions must extend NullableLaunchOptions so the commands mixing it in expose these options");
    }

    private static Set<Field> optionFields() {
        return Arrays.stream(NullableLaunchOptions.class.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Option.class))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Object sampleValueFor(Field option) {
        Class<?> type = option.getType();
        if (type == String.class) {
            return "sample";
        }
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }
        throw new IllegalStateException(String.format(
                "No sample value for option '%s' of type %s: extend this test alongside the new option",
                option.getName(), type.getName()));
    }

    /**
     * Reads the plain getter, which unwraps the JsonNullable, so this test needs no direct dependency on it.
     */
    private static Object readOption(WorkflowLaunchRequest request, String fieldName) throws ReflectiveOperationException {
        String getter = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return WorkflowLaunchRequest.class.getMethod(getter).invoke(request);
    }
}
