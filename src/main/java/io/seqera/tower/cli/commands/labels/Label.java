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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.exceptions.TowerException;
import picocli.CommandLine;

public class Label {
    final String name;

    final String value;

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
                return new Label(key,null);
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
}
