package org.blbulyandavbulyan.filescanner.cli.task;

import org.blbulyandavbulyan.filescanner.cli.service.DirectoryContentService;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;

class FolderProcessingTaskComponentTest {
    private final static Path testDataDirectory = Path.of("src/test/resources/testfolder");
    private final DirectoryContentService directoryContentService = new DirectoryContentService();
    private final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    @Test
    void testFolderProcessingTask() {
        FolderProcessingTask task = new FolderProcessingTask(testDataDirectory, directoryContentService);
        Optional<FolderStatistics> optionalStatistics = forkJoinPool.invoke(task);
        FolderStatistics expectedFolderStatistics = new FolderStatistics();
        expectedFolderStatistics.setDirectoriesCount(BigInteger.valueOf(5));
        expectedFolderStatistics.setTotalFilesSize(BigInteger.valueOf(713));
        expectedFolderStatistics.setFilesCount(BigInteger.valueOf(6));
        assertThat(optionalStatistics).contains(expectedFolderStatistics);
    }
}