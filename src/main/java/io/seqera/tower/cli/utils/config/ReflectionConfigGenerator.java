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

package io.seqera.tower.cli.utils.config;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.*;


/**
 * The GraalVM reflection metadata generation relies on code paths being executed with the tracing agent
 * to dynamically capture various execution paths and update the reflect-config.json.
 * <a href="https://www.graalvm.org/jdk21/reference-manual/native-image/metadata/AutomaticMetadataCollection/#tracing-agent">...</a>
 *
 * This is a utility class that uses reflection to make calls to all classes in the Tower SDK model package,
 * CLI command classes, and CLI response classes, in order to capture reflection metadata for GraalVM native image.
 * Execute this class with the GraalVM tracing agent (see Gradle task 'runReflectionConfigGenerator').
 *
 */
public class ReflectionConfigGenerator {

    private static final String TOWER_MODEL_PACKAGE = "io.seqera.tower.model";
    private static final String CLI_COMMANDS_PACKAGE = "io.seqera.tower.cli.commands";
    private static final String CLI_RESPONSES_PACKAGE = "io.seqera.tower.cli.responses";

    public static void main(String[] args) {
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(TOWER_MODEL_PACKAGE, CLI_COMMANDS_PACKAGE, CLI_RESPONSES_PACKAGE)
                .enableClassInfo()
                .scan()
        ) {
            processAllClasses(scanResult);
        }
    }

    private static void processAllClasses(ScanResult scanResult) {
        for (ClassInfo classInfo : scanResult.getAllClasses()) {
            Class<?> aClass = classInfo.loadClass();
            if (aClass.isEnum()) {
                processEnumClass(aClass);
            } else {
                processRegularClass(aClass);
            }
        }
    }

    private static void processRegularClass(Class<?> aClass) {
        for (Constructor<?> ctor : aClass.getDeclaredConstructors()) {
            ctor.setAccessible(true);
            try {
                Object instance = ctor.newInstance(new Object[ctor.getParameterCount()]);
                accessAllFields(aClass, instance);
                invokeAllInstanceMethods(aClass, instance);
            } catch (Throwable t) {
                System.out.println("Error invoking constructor of class '" + aClass.getName() + "': " + t);
            }
        }
    }

    private static void accessAllFields(Class<?> aClass, Object instance) {
        for (Field field : aClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                field.get(instance);
            } catch (Throwable t) {
                System.out.println("Error accessing field '" + field.getName() + "' of class '" + aClass.getName() + "': " + t);
            }
        }
    }

    private static <E> void processEnumClass(Class<E> enumClass) {
        for (E constant : enumClass.getEnumConstants()) {
            invokeAllInstanceMethods(enumClass, constant);
        }
    }

    private static void invokeAllInstanceMethods(Class<?> aClass, Object instance) {
        for (Method method : aClass.getDeclaredMethods()) {
            invokeMethod(aClass, instance, method);
        }
    }

    private static void invokeMethod(Class<?> aClass, Object instance, Method method) {
        try {
            method.setAccessible(true);
            method.invoke(instance, new Object[method.getParameterCount()]);
        } catch (Throwable t) {
            System.out.println("Error invoking method '" + method.getName() + "' of class '" + aClass.getName() + "': " + t);
        }
    }

}