package com.payment.export.platform.application.scheduler.csv;

import com.payment.export.platform.domain.dto.csv.CsvTransactionRow;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

public class FastCsvStreamWriter {

    private static final String[] HEADER = {
            "transactionId",
            "internalBatchId",
            "batchName",
            "paymentType",
            "batchStatus",
            "accountHolderName",
            "transactionAmount",
            "currencyCode",
            "iban"
    };

    private final Writer writer;

    public FastCsvStreamWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeHeader() throws IOException {
        writeLine(HEADER[0], HEADER[1], HEADER[2], HEADER[3], HEADER[4], HEADER[5], HEADER[6], HEADER[7], HEADER[8]);
    }

    public void writeRow(CsvTransactionRow row) throws IOException {
        writeLine(
                row.transactionId(),
                row.internalBatchId(),
                row.batchName(),
                row.paymentType().name(),
                row.batchStatus().name(),
                row.accountHolderName(),
                toAmount(row.transactionAmount()),
                row.currencyCode(),
                row.iban()
        );
    }

    private String toAmount(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private void writeLine(String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            writeField(values[i]);
        }
        writer.write('\n');
    }

    private void writeField(String value) throws IOException {
        if (value == null) {
            return;
        }

        boolean needsQuoting = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == ',' || current == '"' || current == '\n' || current == '\r') {
                needsQuoting = true;
                break;
            }
        }

        if (!needsQuoting) {
            writer.write(value);
            return;
        }

        writer.write('"');
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '"') {
                writer.write('"');
            }
            writer.write(current);
        }
        writer.write('"');
    }
}

