package com.lordkadoc.batch_partitioner.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    private final JobOperator jobOperator;

    private final Job financialClearingJob;

    public BatchService(JobOperator jobOperator, Job financialClearingJob) {
        this.jobOperator = jobOperator;
        this.financialClearingJob = financialClearingJob;
    }

    public void launchBatch() {
        this.jobOperator.startNextInstance(financialClearingJob);
    }

}
