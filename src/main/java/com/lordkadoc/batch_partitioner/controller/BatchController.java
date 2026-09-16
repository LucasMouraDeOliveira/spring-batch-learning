package com.lordkadoc.batch_partitioner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lordkadoc.batch_partitioner.batch.BatchService;

@RestController
@RequestMapping("launch-batch")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    public void launchBatch() {
        this.batchService.launchBatch();
    }
    
}
