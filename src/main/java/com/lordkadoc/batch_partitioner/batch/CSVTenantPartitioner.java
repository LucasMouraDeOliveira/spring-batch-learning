package com.lordkadoc.batch_partitioner.batch;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;

public class CSVTenantPartitioner implements Partitioner {

    private final Path readDirectory;

    public CSVTenantPartitioner(Path readDirectory) {
        this.readDirectory = readDirectory;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
        int i = 0;
        for (File csvFile : getCSVFiles()) {
            String filename = csvFile.getName();
            String tenantType = filename.split("_")[0];
            ExecutionContext context = new ExecutionContext();
            context.putString("filePath", csvFile.getAbsolutePath());
            context.putString("tenant", tenantType);
            if (tenantType.equalsIgnoreCase("tenantA")) {
                context.putString("delimiter", ";");
            } else {
                context.putString("delimiter", ",");
            }
            map.put("partition_" + i++, context);
        }
        return map;
    }

    private List<File> getCSVFiles() {
        return Arrays.asList(readDirectory.toFile().listFiles((file, name) -> name.endsWith(".csv")));
    }

}
