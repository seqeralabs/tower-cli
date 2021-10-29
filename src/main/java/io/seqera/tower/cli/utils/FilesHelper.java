package io.seqera.tower.cli.utils;

import java.io.BufferedWriter;
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
}
