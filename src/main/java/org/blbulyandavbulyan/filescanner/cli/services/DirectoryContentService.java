package org.blbulyandavbulyan.filescanner.cli.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DirectoryContentService {
    public DirectoryContent getDirectoryContent(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        List<Path> directories = new ArrayList<>();
        try (final var directoryStream = Files.newDirectoryStream(directory)) {
            for (var path : directoryStream) {
                if (Files.isDirectory(path)) {
                    directories.add(path);
                } else {
                    files.add(path);
                }
            }
        }
        return new DirectoryContent(files, directories);
    }
}
