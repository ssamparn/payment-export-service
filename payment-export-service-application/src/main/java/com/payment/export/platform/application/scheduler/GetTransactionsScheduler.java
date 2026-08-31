package com.payment.export.platform.application.scheduler;

import com.payment.export.platform.domain.dto.soap.request.GetTransactionsBatch;
import com.payment.export.platform.domain.dto.soap.request.GetTransactionsRequest;
import com.payment.export.platform.domain.dto.soap.response.GetTransactionsResponse;
import com.payment.export.platform.domain.ports.output.integration.soap.GetTransactionsSoapService;
import com.payment.export.platform.domain.ports.output.repository.GetTransactionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class GetTransactionsScheduler {

	private final GetTransactionsRepository getTransactionsRepository;
	private final GetTransactionsSoapService getTransactionsSoapService;
	private final int maxBatchesPerRun;
	private final int soapPageSize;
	private final int soapCallRetryAttempts;
	private final Duration initialRetryBackoff;
	private final double retryBackoffMultiplier;
	private final Duration maxRetryBackoff;
	private final Duration staleProcessingTimeout;

	public GetTransactionsScheduler(GetTransactionsRepository getTransactionsRepository,
									GetTransactionsSoapService getTransactionsSoapService,
									@Value("${payment-export.scheduler.get-transactions.max-batches-per-run:10}") int maxBatchesPerRun,
									@Value("${payment-export.scheduler.get-transactions.page-size:100}") int soapPageSize,
									@Value("${payment-export.scheduler.get-transactions.soap-call-retry-attempts:3}") int soapCallRetryAttempts,
									@Value("${payment-export.scheduler.get-transactions.initial-retry-backoff:PT1S}") Duration initialRetryBackoff,
									@Value("${payment-export.scheduler.get-transactions.retry-backoff-multiplier:2.0}") double retryBackoffMultiplier,
									@Value("${payment-export.scheduler.get-transactions.max-retry-backoff:PT10S}") Duration maxRetryBackoff,
									@Value("${payment-export.scheduler.get-transactions.stale-processing-timeout:PT15M}") Duration staleProcessingTimeout) {
		this.getTransactionsRepository = getTransactionsRepository;
		this.getTransactionsSoapService = getTransactionsSoapService;
		this.maxBatchesPerRun = Math.max(1, maxBatchesPerRun);
		this.soapPageSize = Math.max(1, soapPageSize);
		this.soapCallRetryAttempts = Math.max(1, soapCallRetryAttempts);
		this.initialRetryBackoff = initialRetryBackoff == null || initialRetryBackoff.isZero() || initialRetryBackoff.isNegative()
				? Duration.ofSeconds(1)
				: initialRetryBackoff;
		this.retryBackoffMultiplier = Math.max(1.0d, retryBackoffMultiplier);
		this.maxRetryBackoff = maxRetryBackoff == null || maxRetryBackoff.isZero() || maxRetryBackoff.isNegative()
				? Duration.ofSeconds(10)
				: maxRetryBackoff;
		this.staleProcessingTimeout = staleProcessingTimeout == null || staleProcessingTimeout.isZero() || staleProcessingTimeout.isNegative()
				? Duration.ofMinutes(15)
				: staleProcessingTimeout;
	}

	@Scheduled(fixedDelayString = "${payment-export.scheduler.get-transactions.fixed-delay:60000}")
	public void fetchTransactions() {
		for (GetTransactionsBatch transactionBatch : getTransactionsRepository.findBatchesForTransactionFetch(maxBatchesPerRun, soapPageSize, staleProcessingTimeout)) {
			processBatch(transactionBatch);
		}
	}

	private void processBatch(GetTransactionsBatch transactionBatch) {

		try {
			GetTransactionsRequest currentRequest = transactionBatch.request();
			boolean moreResultsAvailable;

			do {
				GetTransactionsResponse response = callSoapWithRetry(transactionBatch.batchId(), currentRequest);
				getTransactionsRepository.saveTransactionPage(transactionBatch.batchId(), response);
				moreResultsAvailable = response.moreResultsAvailable();
				currentRequest = currentRequest.nextPage();
			} while (moreResultsAvailable);

			getTransactionsRepository.markBatchAsCompleted(transactionBatch.batchId());
		} catch (Exception exception) {
			handleTransactionFetchFailure(transactionBatch, exception);
		}
	}

	private GetTransactionsResponse callSoapWithRetry(java.util.UUID batchId, GetTransactionsRequest request) {
		Duration backoff = initialRetryBackoff;

		for (int attempt = 1; attempt <= soapCallRetryAttempts; attempt++) {
			try {
				return getTransactionsSoapService.call(request);
			} catch (Exception exception) {
				if (attempt >= soapCallRetryAttempts) {
					throw exception;
				}

				log.warn("SOAP get-transactions call failed for batch {} on page {}. Retrying attempt {}/{} after {} ms",
						batchId,
						request.page(),
						attempt + 1,
						soapCallRetryAttempts,
						backoff.toMillis(),
						exception);
				sleep(backoff);
				backoff = nextBackoff(backoff);
			}
		}

		throw new IllegalStateException("SOAP retry loop terminated unexpectedly");
	}

	private void handleTransactionFetchFailure(GetTransactionsBatch transactionBatch, Exception exception) {
		log.error("Failed to fetch transactions for batch {}", transactionBatch.batchId(), exception);
		getTransactionsRepository.markBatchAsFailed(transactionBatch.batchId(), exception.getMessage());
		getTransactionsRepository.markJobAsFailed(transactionBatch.jobId(), exception.getMessage());
	}

	private Duration nextBackoff(Duration currentBackoff) {
		long nextMillis = Math.round(currentBackoff.toMillis() * retryBackoffMultiplier);
		long boundedMillis = Math.clamp(nextMillis, 1L, maxRetryBackoff.toMillis());
		return Duration.ofMillis(boundedMillis);
	}

	private void sleep(Duration duration) {
		try {
			Thread.sleep(Math.max(1L, duration.toMillis()));
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting to retry SOAP transaction fetch", interruptedException);
		}
	}
}
