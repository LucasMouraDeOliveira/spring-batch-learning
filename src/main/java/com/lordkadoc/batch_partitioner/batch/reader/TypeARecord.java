package com.lordkadoc.batch_partitioner.batch.reader;

public record TypeARecord(Long id, String name, double value, boolean disabledAccount) implements TransactionRecord {}
