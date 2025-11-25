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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.LabelType;
import picocli.CommandLine;

public class Label {
    public final String name;

    public final String value;

    public Label(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return String.format("%s=%s", this.name, this.value);
        } else {
            return this.name;
        }
    }

    public boolean matches(String name, String value) {
        return this.name.equals(name) &&
                (this.value == null || this.value.equals(value));
    }

    public LabelType getType() {
        return value == null ? LabelType.simple : LabelType.resource;
    }

    public static class LabelConverter implements CommandLine.ITypeConverter<Label> {

        @Override
        public Label convert(String rawValue) throws Exception {
            String[] parts = rawValue.split("=");
            if (parts.length > 2) {
                throw new TowerException("Invalid label format");
            }
            final String key = parts[0];
            if (key.isBlank()) {
                throw new TowerException("Label key cannot be empty");
            }
            if (parts.length == 1) {
                return new Label(key, null);
            }
            final String value = parts[1];
            if (value.isBlank()) {
                throw new TowerException("Label value cannot be empty");
            }
            return new Label(key, value);
        }
    }

    public static class ResourceLabelsConverter extends LabelConverter {
        @Override
        public Label convert(String rawValue) throws Exception {
            Label label = super.convert(rawValue);
            if (label.value == null) {
                throw new TowerException("Label value is required");
            }
            return label;
        }
    }

    public static class StudioResourceLabelsConverter extends ResourceLabelsConverter {
        @Override
        public Label convert(String rawValue) throws Exception {
            // allow for a blank/null value to be able to send empty list of labels instead of null
            if (rawValue == null || rawValue.isBlank()) {
                return null;
            }
            return super.convert(rawValue);
        }
    }
}
