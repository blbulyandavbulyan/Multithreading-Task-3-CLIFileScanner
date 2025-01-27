package org.blbulyandavbulyan.filescanner.cli.task;

import lombok.Getter;
import lombok.extern.java.Log;
import org.blbulyandavbulyan.filescanner.cli.service.DirectoryContentService;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;

/**
 * Task which will calculate files sizes, files and folders count
 * If IOException occurred, it will ignore it, just write it to log as warning and then use default value
 */
@Log
public class FolderProcessingTask extends RecursiveTask<Optional<FolderStatistics>> {
    @Getter
    private final Path pathToProcessingDirectory;
    private final DirectoryContentService directoryContentService;

    public FolderProcessingTask(Path pathToProcessingDirectory, DirectoryContentService directoryContentService) {
        if (!Files.isDirectory(pathToProcessingDirectory)) {
            throw new IllegalArgumentException("The specified path %s should point to a directory".formatted(pathToProcessingDirectory));
        }
        this.pathToProcessingDirectory = pathToProcessingDirectory;
        this.directoryContentService = directoryContentService;
    }


    @Override
    protected Optional<FolderStatistics> compute() {
        log.info(() -> "Starting to process " + pathToProcessingDirectory);
        FolderStatistics folderStatistics = new FolderStatistics();
        List<Path> files;
        List<FolderProcessingTask> subdirectoryTasks;
        try {
            final var directoryContent = directoryContentService.getDirectoryContent(pathToProcessingDirectory);
            folderStatistics.setDirectoriesCount(BigInteger.valueOf(directoryContent.directories().size()));
            subdirectoryTasks = createTasksForSubdirectories(directoryContent.directories());
            files = directoryContent.files();
        } catch (IOException e) {
            //in case of error related to reading the directory, we just ignore this directory and don't add it to the result
            log.log(Level.WARNING, "Failed to process " + pathToProcessingDirectory, e);
            return Optional.empty();
        }
        BigInteger filesCount = BigInteger.valueOf(files.size());
        log.finest(() -> "Found %s files in %s".formatted(filesCount, pathToProcessingDirectory));
        folderStatistics.setTotalFilesSize(calculateFileSizesIgnoringErrors(files));
        folderStatistics.setFilesCount(filesCount);
        folderStatistics.add(joinSubdirectoriesTasks(subdirectoryTasks));
        log.info(() -> "Finished processing " + pathToProcessingDirectory);
        return Optional.of(folderStatistics);
    }

    private FolderStatistics joinSubdirectoriesTasks(List<FolderProcessingTask> subdirectoryTasks) {
        FolderStatistics folderStatistics = new FolderStatistics();
        long countOfProcessedDirectories = 0;
        int size = subdirectoryTasks.size();
        for (final var task : subdirectoryTasks) {
            long finalCountOfProcessedDirectories = countOfProcessedDirectories;
            log.info(() -> "%d/%d directories remaining to finish processing of %s"
                    .formatted(finalCountOfProcessedDirectories, size, pathToProcessingDirectory));
            log.finest(() -> "Waiting for subdirectory %s to finish".formatted(task.getPathToProcessingDirectory()));
            task.join().ifPresent(folderStatistics::add);
            countOfProcessedDirectories++;
        }
        return folderStatistics;
    }

    private List<FolderProcessingTask> createTasksForSubdirectories(List<Path> directories) {
        return directories
                .stream()
                .map(directory -> new FolderProcessingTask(directory, directoryContentService))
                .map(FolderProcessingTask::fork)
                .map(FolderProcessingTask.class::cast)//casting because fork returns ForkJoinTask
                .toList();
    }

    private BigInteger calculateFileSizesIgnoringErrors(List<Path> files) {
        log.finest(() -> "Calculating sizes of files in " + pathToProcessingDirectory);
        BigInteger result = BigInteger.ZERO;
        for (Path file : files) {
            try {
                result = result.add(BigInteger.valueOf(Files.size(file)));
            } catch (IOException e) {
                //we saw nothing, ignore the file and move on
                log.log(Level.WARNING, e, () -> "Failed to get size for file " + file);
            }
        }
        log.finest(() -> "Finished calculating sizes of files in " + pathToProcessingDirectory);
        return result;
    }
}
