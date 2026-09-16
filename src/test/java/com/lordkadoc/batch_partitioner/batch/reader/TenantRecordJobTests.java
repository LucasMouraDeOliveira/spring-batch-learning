package com.lordkadoc.batch_partitioner.batch.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;

import com.lordkadoc.batch_partitioner.batch.processor.TransactionProcessor;
import com.lordkadoc.batch_partitioner.batch.writer.ValidatedTransactionRecord;

class TenantRecordJobTests {

    @TempDir
    Path temporaryDirectory;

    private final ReaderConfiguration readerConfiguration = new ReaderConfiguration();
    private final TransactionProcessor transactionProcessor = new TransactionProcessor();

    @Test
    void readsAndProcessesTenantARecords() throws Exception {
        Path inputFile = writeInputFile("tenantA_records.csv", "typeA;15489;Homer;527.45\n"
                + "typeB;account51561;849.18;742 Evergreen Terrace\n");

        FlatFileItemReader<TransactionRecord> reader = openReader(inputFile, ";");
        try {
            assertEquals(new TypeARecord(15489L, "Homer", 527.45, false), reader.read());
            assertEquals(new TypeBRecord("account51561", 849.18, "742 Evergreen Terrace"), reader.read());
        } finally {
            reader.close();
        }
    }

    @Test
    void readsAndProcessesTenantBRecords() throws Exception {
        Path inputFile = writeInputFile("tenantB_records.csv", "typeA,15489,Homer,527.45\n"
                + "typeB,account51561,849.18,742 Evergreen Terrace\n");

        FlatFileItemReader<TransactionRecord> reader = openReader(inputFile, ",");
        try {
            assertEquals(new TypeARecord(15489L, "Homer", 527.45, false), reader.read());
            TypeBRecord record = assertInstanceOf(TypeBRecord.class, reader.read());
            assertEquals(new TypeBRecord("account51561", 849.18, "742 Evergreen Terrace"), record);
            assertEquals(new ValidatedTransactionRecord(
                    "account51561", null, "742 Evergreen Terrace", 849.18, false),
                    transactionProcessor.process(record));
                } finally {
                    reader.close();
        }
    }

    @Test
    void readerReachesEndOfTenantFile() throws Exception {
        Path inputFile = writeInputFile("tenant_records.csv", "typeA;1;Alice;10.00\n");

        FlatFileItemReader<TransactionRecord> reader = openReader(inputFile, ";");
        try {
            assertInstanceOf(TypeARecord.class, reader.read());
            assertNull(reader.read());
        } finally {
            reader.close();
        }
    }

    @Test
    void amountsBelowSixHundredAreAuthorized() throws Exception {
        assertTrue(transactionProcessor.process(new TypeARecord(1L, "A", 599.99, false)).authorized());
        assertFalse(transactionProcessor.process(new TypeBRecord("B", 600.00, "Address")).authorized());
    }

    private Path writeInputFile(String filename, String contents) throws Exception {
        return Files.writeString(temporaryDirectory.resolve(filename), contents);
    }

    private FlatFileItemReader<TransactionRecord> openReader(Path inputFile, String delimiter) throws Exception {
        FlatFileItemReader<TransactionRecord> reader = readerConfiguration.tenantItemReader(
                inputFile.toAbsolutePath().toString(), delimiter);
        reader.open(new ExecutionContext());
        return reader;
    }
}