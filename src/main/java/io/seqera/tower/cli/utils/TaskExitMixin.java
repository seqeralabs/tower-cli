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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Jackson mixin for {@link io.seqera.tower.model.Task} that applies lenient
 * integer deserialization to the {@code exit} field.
 * <p>
 * The Platform API returns the task exit code as a quoted string ({@code "exit": "0"})
 * while the SDK model declares it as {@code Integer}. This mixin narrows the
 * workaround to only the affected field.
 *
 * FIXME: Workaround for Platform versions before 26.x. Remove once those versions are phased out (see #578).
 */
public abstract class TaskExitMixin {

    @JsonDeserialize(using = LenientIntegerDeserializer.class)
    abstract Integer getExit();
}
