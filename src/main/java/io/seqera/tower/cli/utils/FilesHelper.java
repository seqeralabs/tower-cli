package io.seqera.tower.cli.utils;

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
}
