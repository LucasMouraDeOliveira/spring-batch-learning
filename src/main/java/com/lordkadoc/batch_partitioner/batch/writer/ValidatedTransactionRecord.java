package com.lordkadoc.batch_partitioner.batch.writer;

public record ValidatedTransactionRecord(String id, String name, String address, double amount, boolean authorized) {}
