package com.lordkadoc.batch_partitioner.batch.writer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;

@Configuration
public class WriterConfiguration {

    @Bean
    @StepScope
    FlatFileItemWriter<ValidatedTransactionRecord> tenantItemWriter(
         @Value("#{stepExecutionContext['filePath']}") String filePath
    ) throws IOException {
        BeanWrapperFieldExtractor<ValidatedTransactionRecord> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] { "id", "name", "address", "amount", "authorized" });

        DelimitedLineAggregator<ValidatedTransactionRecord> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<ValidatedTransactionRecord>()
                .name("tenantItemWriter")
                .resource(getDestination(filePath))
                .lineAggregator(lineAggregator)
                .build();
    }

    private WritableResource getDestination(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource("financial-records/out");
        Path sourcePath = Paths.get(filePath);
        String filename = sourcePath.getFileName().toString();
        filename = filename.substring(0, filename.lastIndexOf("."));
        String destinationFile = String.format("%s_%s.csv", filename, System.currentTimeMillis());
        return new FileSystemResource(resource.getFile().getAbsolutePath() + "/" + destinationFile);
    }

}
