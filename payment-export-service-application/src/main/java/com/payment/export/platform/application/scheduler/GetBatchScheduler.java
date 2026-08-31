package com.payment.export.platform.application.scheduler;

import com.payment.export.platform.domain.dto.soap.request.GetBatchJob;
import com.payment.export.platform.domain.dto.soap.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;
import com.payment.export.platform.domain.ports.output.integration.soap.GetBatchSoapService;
import com.payment.export.platform.domain.ports.output.repository.GetBatchRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class GetBatchScheduler {

	private final GetBatchRepository getBatchRepository;
	private final GetBatchSoapService getBatchSoapService;
	private final int maxJobsPerRun;
	private final int soapPageSize;
	private final int maxJobRetryAttempts;
	private final int soapCallRetryAttempts;
	private final Duration initialRetryBackoff;
	private final double retryBackoffMultiplier;
	private final Duration maxRetryBackoff;
	private final Duration staleFetchTimeout;

	public GetBatchScheduler(GetBatchRepository getBatchRepository,
							 GetBatchSoapService getBatchSoapService,
							 @Value("${payment-export.scheduler.get-batch.max-jobs-per-run:10}") int maxJobsPerRun,
							 @Value("${payment-export.scheduler.get-batch.page-size:100}") int soapPageSize,
							 @Value("${payment-export.scheduler.get-batch.max-job-retry-attempts:5}") int maxJobRetryAttempts,
							 @Value("${payment-export.scheduler.get-batch.soap-call-retry-attempts:3}") int soapCallRetryAttempts,
							 @Value("${payment-export.scheduler.get-batch.initial-retry-backoff:PT1S}") Duration initialRetryBackoff,
							 @Value("${payment-export.scheduler.get-batch.retry-backoff-multiplier:2.0}") double retryBackoffMultiplier,
							 @Value("${payment-export.scheduler.get-batch.max-retry-backoff:PT10S}") Duration maxRetryBackoff,
							 @Value("${payment-export.scheduler.get-batch.stale-fetch-timeout:PT15M}") Duration staleFetchTimeout) {
		this.getBatchRepository = getBatchRepository;
		this.getBatchSoapService = getBatchSoapService;
		this.maxJobsPerRun = Math.max(1, maxJobsPerRun);
		this.soapPageSize = Math.max(1, soapPageSize);
		this.maxJobRetryAttempts = Math.max(1, maxJobRetryAttempts);
		this.soapCallRetryAttempts = Math.max(1, soapCallRetryAttempts);
		this.initialRetryBackoff = initialRetryBackoff == null || initialRetryBackoff.isNegative() || initialRetryBackoff.isZero()
				? Duration.ofSeconds(1)
				: initialRetryBackoff;
		this.retryBackoffMultiplier = Math.max(1.0d, retryBackoffMultiplier);
		this.maxRetryBackoff = maxRetryBackoff == null || maxRetryBackoff.isNegative() || maxRetryBackoff.isZero()
				? Duration.ofSeconds(10)
				: maxRetryBackoff;
		this.staleFetchTimeout = staleFetchTimeout == null || staleFetchTimeout.isNegative() || staleFetchTimeout.isZero()
				? Duration.ofMinutes(15)
				: staleFetchTimeout;
	}

	@Scheduled(fixedDelayString = "${payment-export.scheduler.get-batch.fixed-delay:60000}")
	@SchedulerLock(name = "GetBatchScheduler.fetchBatches")
	public void fetchBatches() {
		for (GetBatchJob batchJob : getBatchRepository.findJobsForBatchFetch(maxJobsPerRun, soapPageSize, staleFetchTimeout)) {
			processJob(batchJob);
		}
	}

	private void processJob(GetBatchJob batchJob) {
		GetBatchRequest initialRequest = batchJob.request();
		var jobId = batchJob.jobId();
		if (!getBatchRepository.markJobAsFetchingBatches(jobId, staleFetchTimeout)) {
			log.debug("Skipping job {} because it is no longer eligible for batch fetching", jobId);
			return;
		}

		try {
			if (batchJob.isBatchFetchComplete()) {
				getBatchRepository.markJobAsBatchesFetched(jobId);
				return;
			}

			GetBatchRequest currentRequest = initialRequest;
			boolean moreResultsAvailable;
			do {
				GetBatchResponse response = callSoapWithRetry(jobId, currentRequest);
				getBatchRepository.saveBatchPage(jobId, response);

				moreResultsAvailable = response.moreResultsAvailable();
				currentRequest = currentRequest.nextPage();
			} while (moreResultsAvailable);

			getBatchRepository.markJobAsBatchesFetched(jobId);
		} catch (Exception exception) {
			handleBatchFetchFailure(batchJob, exception);
		}
	}

	private GetBatchResponse callSoapWithRetry(java.util.UUID jobId, GetBatchRequest request) {
		Duration backoff = initialRetryBackoff;

		for (int attempt = 1; attempt <= soapCallRetryAttempts; attempt++) {
			try {
				return getBatchSoapService.call(request);
			} catch (Exception exception) {
				if (attempt >= soapCallRetryAttempts) {
					throw exception;
				}

				log.warn("SOAP get-batch call failed for job {} on page {}. Retrying attempt {}/{} after {} ms",
						jobId,
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

	private void handleBatchFetchFailure(GetBatchJob batchJob, Exception exception) {
		var jobId = batchJob.jobId();
		int nextRetryCount = batchJob.retryCount() + 1;
		log.error("Failed to fetch batches for job {} on retry {}/{}",
				jobId,
				nextRetryCount,
				maxJobRetryAttempts,
				exception);

		if (nextRetryCount >= maxJobRetryAttempts) {
			getBatchRepository.markJobAsFailed(jobId, exception.getMessage());
			return;
		}

		getBatchRepository.markJobAsBatchesFetchFailed(jobId, exception.getMessage());
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
			throw new IllegalStateException("Interrupted while waiting to retry SOAP batch fetch", interruptedException);
		}
	}
}
