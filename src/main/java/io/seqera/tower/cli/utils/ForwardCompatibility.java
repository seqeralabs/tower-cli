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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;

import java.util.Collection;
import java.util.List;

/**
 * Makes the mapper used to read Platform API responses tolerate values that were
 * added to the API after this CLI version was released, instead of failing the
 * whole command:
 *
 * - An unknown discriminator (e.g. a new credentials or compute platform type)
 *   is read as the polymorphic base class, keeping the {@code discriminator}
 *   value and dropping the type specific fields;
 * - An unknown enum value is read as {@code null}.
 *
 * Only apply it to the API client mapper, that is, to server-sent responses:
 * files provided by the user must keep failing on unknown values.
 */
public final class ForwardCompatibility {

    private ForwardCompatibility() {
    }

    public static ObjectMapper configure(ObjectMapper mapper) {
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        mapper.addHandler(new UnknownSubtypeHandler());
        mapper.setSubtypeResolver(new AssignableSubtypeResolver());
        return mapper;
    }

    /**
     * Reads an object whose discriminator is not known by this CLI version as its
     * polymorphic base class (e.g. {@code SecurityKeys}) instead of failing.
     *
     * The {@code discriminator} value is kept and the type specific fields are dropped.
     */
    private static class UnknownSubtypeHandler extends DeserializationProblemHandler {

        @Override
        public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId, TypeIdResolver idResolver, String failureMsg) {
            // Returning null keeps the default behaviour (fail) for bases that cannot be instantiated.
            return baseType.isConcrete() ? baseType : null;
        }
    }

    /**
     * Ignores the classes that the SDK lists as subtypes of a base class but that do not extend it.
     *
     * For example, supposing the SDK listed {@code MyProviderSecurityKeys} as a subtype of
     * {@code SecurityKeys} under the {@code my_provider} discriminator, but the class did not extend
     * {@code SecurityKeys}, Jackson would fail with "Class MyProviderSecurityKeys not subtype of
     * SecurityKeys" when a response contained that discriminator; ignoring the class turns it into
     * an unknown discriminator, handled by {@link UnknownSubtypeHandler}.
     */
    private static class AssignableSubtypeResolver extends StdSubtypeResolver {

        @Override
        public Collection<NamedType> collectAndResolveSubtypesByTypeId(MapperConfig<?> config, AnnotatedMember property, JavaType baseType) {
            Class<?> base = baseType != null ? baseType.getRawClass() : property.getRawType();
            return assignableTo(base, super.collectAndResolveSubtypesByTypeId(config, property, baseType));
        }

        @Override
        public Collection<NamedType> collectAndResolveSubtypesByTypeId(MapperConfig<?> config, AnnotatedClass baseType) {
            return assignableTo(baseType.getRawType(), super.collectAndResolveSubtypesByTypeId(config, baseType));
        }

        private static List<NamedType> assignableTo(Class<?> base, Collection<NamedType> subtypes) {
            return subtypes.stream().filter(t -> base.isAssignableFrom(t.getType())).toList();
        }
    }

}
