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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesHelper {

    private FilesHelper() {
    }

    public static String readString(Path path) throws IOException {
        if (path == null) {
            return null;
        }
        return Files.readString(path);
    }

    public static void saveString(String fileName, String text) {
        try {
            BufferedWriter writer;
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFileAndDelete(File file) {
        String outcome = null;

        try {
            outcome = Files.readString(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            File obj = new File(file.getPath());
            obj.delete();
        }

        return outcome;
    }
}
