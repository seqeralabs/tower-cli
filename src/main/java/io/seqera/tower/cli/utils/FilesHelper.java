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
