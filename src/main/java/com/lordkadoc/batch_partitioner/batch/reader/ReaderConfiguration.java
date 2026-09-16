package com.lordkadoc.batch_partitioner.batch.reader;

import java.io.File;
import java.util.HashMap;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.infrastructure.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class ReaderConfiguration {

    @Bean
    @StepScope
    FlatFileItemReader<TransactionRecord> tenantItemReader(
            @Value("#{stepExecutionContext['filePath']}") String filePath,
            @Value("#{stepExecutionContext['delimiter']}") String delimiter) {
        FileSystemResource resource = new FileSystemResource(new File(filePath));
        return new FlatFileItemReaderBuilder<TransactionRecord>()
                .name(filePath)
                .resource(resource)
                .lineMapper(patternMatchingMapper(delimiter))
                .build();
    }

    LineMapper<TransactionRecord> patternMatchingMapper(String delimiter) {
        var tokenizers = new HashMap<String, LineTokenizer>();
        tokenizers.put("typeA*", typeATokenizer(delimiter));
        tokenizers.put("typeB*", typeBTokenizer(delimiter));

        var fieldSetMappers = new HashMap<String, FieldSetMapper<TransactionRecord>>();
        fieldSetMappers.put("typeA*", typeAMapper());
        fieldSetMappers.put("typeB*", typeBMapper());

        return new PatternMatchingCompositeLineMapper<TransactionRecord>(tokenizers, fieldSetMappers);
    }

    LineTokenizer typeATokenizer(String delimiter) {
        var tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(delimiter);
        tokenizer.setNames("type", "id", "name", "value", "disabledAccount");
        return tokenizer;
    }

    LineTokenizer typeBTokenizer(String delimiter) {
        var tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(delimiter);
        tokenizer.setNames("type", "id", "value", "city");
        return tokenizer;
    }

    FieldSetMapper<TransactionRecord> typeAMapper() {
        return fieldSet -> new TypeARecord(
                fieldSet.readLong("id"),
                fieldSet.readString("name"),
                fieldSet.readDouble("value"),
                fieldSet.readBoolean("disabledAccount"));
    }

    FieldSetMapper<TransactionRecord> typeBMapper() {
        return fieldSet -> new TypeBRecord(
                fieldSet.readString("id"),
                fieldSet.readDouble("value"),
                fieldSet.readString("city"));
    }

}
