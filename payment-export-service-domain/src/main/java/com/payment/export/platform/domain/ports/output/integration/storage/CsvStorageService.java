package com.payment.export.platform.domain.ports.output.integration.storage;

import java.io.InputStream;
import java.util.UUID;

public interface CsvStorageService {

    String upload(UUID jobId, String objectKey, InputStream csvContent, long contentLength);
}

