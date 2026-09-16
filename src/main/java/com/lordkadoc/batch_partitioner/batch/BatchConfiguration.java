package com.lordkadoc.batch_partitioner.batch;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;

import com.lordkadoc.batch_partitioner.batch.processor.TransactionProcessor;
import com.lordkadoc.batch_partitioner.batch.reader.TransactionRecord;
import com.lordkadoc.batch_partitioner.batch.writer.ValidatedTransactionRecord;

@Configuration
public class BatchConfiguration {

    @Bean
    public Job financialClearingJob(JobRepository jobRepository, Step partitioningStep) {
        return new JobBuilder(jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(partitioningStep)
                .build();
    }

    @Bean
    public Step partitioningStep(JobRepository jobRepository, Step step1, TaskExecutor taskExecutor)
            throws IOException {
        return new StepBuilder("step1.manager", jobRepository)
                .partitioner("step1", tenantPartitioner())
                .step(step1)
                .gridSize(10)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step step1(
            JobRepository jobRepository, 
            FlatFileItemReader<TransactionRecord> tenantItemReader,
            TransactionProcessor tenantItemProcessor,
            FlatFileItemWriter<ValidatedTransactionRecord> tenantItemWriter
    ) {
        return new StepBuilder(jobRepository)
                .<TransactionRecord, ValidatedTransactionRecord>chunk(1)
                .reader(tenantItemReader)
                .processor(tenantItemProcessor)
                .writer(tenantItemWriter)
                .build();
    }

    @Bean
    public CSVTenantPartitioner tenantPartitioner() throws IOException {
        ClassPathResource resource = new ClassPathResource("financial-records/in");
        Path readDirectory = resource.getFilePath();
        return new CSVTenantPartitioner(readDirectory);
    }

}
