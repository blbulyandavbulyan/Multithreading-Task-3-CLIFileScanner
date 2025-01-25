package org.blbulyandavbulyan.filescanner.cli.services;

import java.nio.file.Path;
import java.util.List;

public record DirectoryContent(List<Path> files, List<Path> directories) {
}