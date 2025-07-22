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
 * in order to capture methods reflection metadata of any new fields/classes in Tower SDK.
 * Execute this class with the GraalVM tracing agent (see gradle task 'runReflectionConfigGenerator').
 *
 */
public class ReflectionConfigGenerator {

    private static final String TOWER_MODEL_PACKAGE = "io.seqera.tower.model";

    public static void main(String[] args) {

        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(TOWER_MODEL_PACKAGE)
                .enableClassInfo()
                .scan()) {

            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                Class<?> clazz = classInfo.loadClass();

                for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
                    ctor.setAccessible(true);
                    try {
                        Object instance = ctor.newInstance(new Object[ctor.getParameterCount()]);
                        for (Method method : clazz.getDeclaredMethods()) {
                            method.setAccessible(true);
                            method.invoke(instance, new Object[method.getParameterCount()]);
                        }
                    } catch (Throwable t) {
                        System.out.println("Error on " + clazz.getName() + ": " + t);
                    }
                }
            }
        }
    }
}