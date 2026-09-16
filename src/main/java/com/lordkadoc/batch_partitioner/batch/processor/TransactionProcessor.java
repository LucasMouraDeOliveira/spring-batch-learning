package com.lordkadoc.batch_partitioner.batch.processor;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.lordkadoc.batch_partitioner.batch.reader.TransactionRecord;
import com.lordkadoc.batch_partitioner.batch.reader.TypeARecord;
import com.lordkadoc.batch_partitioner.batch.reader.TypeBRecord;
import com.lordkadoc.batch_partitioner.batch.writer.ValidatedTransactionRecord;


@Component
@StepScope
public class TransactionProcessor implements ItemProcessor<TransactionRecord, ValidatedTransactionRecord> {

    @Override
    public @Nullable ValidatedTransactionRecord process(TransactionRecord item) throws Exception {
        switch(item) {
            case TypeARecord typeA -> {
                if (typeA.disabledAccount()) {
                    return null; // Skip processing for disabled accounts
                }
                return new ValidatedTransactionRecord(typeA.id().toString(), typeA.name(), null, typeA.value(), isAmountAuthorized(typeA.value()));
            }
            case TypeBRecord typeB -> {
                return new ValidatedTransactionRecord(typeB.id().toString(), null, typeB.city(), typeB.value(), isAmountAuthorized(typeB.value()));
            }
            default -> throw new IllegalArgumentException("Unknown transaction record type: " + item.getClass().getName());
        }
    }

    private boolean isAmountAuthorized(double amount) {
        return amount < 600; // Example: authorize if amount is less than 600
    }

}
