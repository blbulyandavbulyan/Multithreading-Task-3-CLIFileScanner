package org.blbulyandavbulyan.filescanner.cli.task;

import lombok.Getter;
import lombok.extern.java.Log;
import org.blbulyandavbulyan.filescanner.cli.services.DirectoryContentService;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;

@Log
public class FolderProcessingTask extends RecursiveTask<Optional<Statistics>> {
    @Getter
    private final Path pathToProcessingDirectory;
    private final DirectoryContentService directoryContentService;

    public FolderProcessingTask(Path pathToProcessingDirectory, DirectoryContentService directoryContentService) {
        if (!Files.isDirectory(pathToProcessingDirectory)) {
            throw new IllegalArgumentException("The specified path %s should point to a directory" + pathToProcessingDirectory);
        }
        this.pathToProcessingDirectory = pathToProcessingDirectory;
        this.directoryContentService = directoryContentService;
    }


    @Override
    protected Optional<Statistics> compute() {
        log.info(() -> "Starting to process " + pathToProcessingDirectory);
        try {
            Statistics statistics = new Statistics();
            List<Path> files;
            List<FolderProcessingTask> subdirectoryTasks;
            {
                final var directoryContent = directoryContentService.getDirectoryContent(pathToProcessingDirectory);
                statistics.setDirectoriesCount(BigInteger.valueOf(directoryContent.directories().size()));
                subdirectoryTasks = createTasksForSubdirectories(directoryContent.directories());
                files = directoryContent.files();
            }
            BigInteger filesCount = BigInteger.valueOf(files.size());
            log.finest(()-> "Found %s files in %s".formatted(filesCount, pathToProcessingDirectory));
            BigInteger totalFilesSize = calculateFileSizes(files);
            statistics.setTotalSize(totalFilesSize);
            statistics.setFilesCount(filesCount);
            long countOfProcessedDirectories = 0;
            int size = subdirectoryTasks.size();
            for (final var task : subdirectoryTasks) {
                long finalCountOfProcessedDirectories = countOfProcessedDirectories;
                log.info(()-> "%d/%d directories remaining to finish processing of %s".formatted(finalCountOfProcessedDirectories, size, pathToProcessingDirectory));
                log.finest(() -> "Waiting for subdirectory %s to finish".formatted(task.getPathToProcessingDirectory()));
                task.join().ifPresent(statistics::add);
                countOfProcessedDirectories++;
            }

            log.info("Finished processing " + pathToProcessingDirectory);
            return Optional.of(statistics);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to process " + pathToProcessingDirectory, e);
            return Optional.empty();
        }
    }

    private List<FolderProcessingTask> createTasksForSubdirectories(List<Path> directories) {
        return directories
                .stream()
                .map(directory -> new FolderProcessingTask(directory, directoryContentService))
                .map(FolderProcessingTask::fork)
                .map(FolderProcessingTask.class::cast)
                .toList();
    }

    private BigInteger calculateFileSizes(List<Path> files) {
        log.finest(() -> "Calculating sizes of files in " + pathToProcessingDirectory);
        BigInteger result = BigInteger.ZERO;
        for (Path file : files) {
            result = result.add(BigInteger.valueOf(getFileSizeOrLogError(file)));
        }
        log.finest(() -> "Finished calculating sizes of files in " + pathToProcessingDirectory);
        return result;
    }

    private static long getFileSizeOrLogError(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to get size for file " + file, e);
            return 0;
        }
    }
}
