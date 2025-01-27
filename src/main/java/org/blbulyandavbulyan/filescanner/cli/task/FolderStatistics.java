package org.blbulyandavbulyan.filescanner.cli.task;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

/**
 * Represents result of processing folder, gives files count, directories count, and total files size
 * This class uses BigIntegers, because I thought that sometimes these numbers can be pretty big, if we start from the root of our file system
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FolderStatistics {
    /**
     * Files count in all subdirectories and current directory
     */
    private BigInteger filesCount = BigInteger.ZERO;
    /**
     * Count of subdirectories (including sub subsubdirectories, and etc.)
     */
    private BigInteger directoriesCount = BigInteger.ZERO;
    /**
     * Sum of file sizes in bytes
     */
    private BigInteger totalFilesSize = BigInteger.ZERO;

    public void add(FolderStatistics folderStatistics) {
        totalFilesSize = totalFilesSize.add(folderStatistics.getTotalFilesSize());
        filesCount = filesCount.add(folderStatistics.getFilesCount());
        directoriesCount = directoriesCount.add(folderStatistics.getDirectoriesCount());
    }

}
