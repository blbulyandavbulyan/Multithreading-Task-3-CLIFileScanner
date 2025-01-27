package org.blbulyandavbulyan.filescanner.cli.services;

import java.nio.file.Path;
import java.util.List;

/**
 * Representation of directory content
 * @param files contained in a given directory
 * @param directories contained in a given directory
 */
public record DirectoryContent(List<Path> files, List<Path> directories) {
}