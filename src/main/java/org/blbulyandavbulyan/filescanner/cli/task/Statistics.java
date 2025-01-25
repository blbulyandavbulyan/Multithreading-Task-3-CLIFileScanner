package org.blbulyandavbulyan.filescanner.cli.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@Setter
@ToString
public class Statistics {
    private BigInteger filesCount = BigInteger.ZERO;
    private BigInteger directoriesCount = BigInteger.ZERO;
    private BigInteger totalSize = BigInteger.ZERO;

    public void add(Statistics statistics) {
        totalSize = totalSize.add(statistics.getTotalSize());
        filesCount = filesCount.add(statistics.getFilesCount());
        directoriesCount = directoriesCount.add(statistics.getDirectoriesCount());
    }

}
