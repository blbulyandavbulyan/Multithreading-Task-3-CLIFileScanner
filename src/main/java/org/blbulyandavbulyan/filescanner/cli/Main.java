package org.blbulyandavbulyan.filescanner.cli;

import lombok.extern.java.Log;
import org.blbulyandavbulyan.filescanner.cli.service.DirectoryContentService;
import org.blbulyandavbulyan.filescanner.cli.task.FolderProcessingTask;

import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

@Log
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Wrong number of arguments, required: path to directory");
        }
        Path directory = Path.of(args[0]);
        ForkJoinPool.commonPool()
                .invoke(new FolderProcessingTask(directory, new DirectoryContentService()))
                .ifPresent(statistics -> {
                    log.info("Program finished, results: " + statistics);
                });
    }
}