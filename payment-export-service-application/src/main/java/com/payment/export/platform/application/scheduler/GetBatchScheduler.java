package com.payment.export.platform.application.scheduler;

import com.payment.export.platform.domain.dto.soap.request.GetBatchJob;
import com.payment.export.platform.domain.dto.soap.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;
import com.payment.export.platform.domain.ports.output.integration.soap.GetBatchSoapService;
import com.payment.export.platform.domain.ports.output.repository.GetBatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GetBatchScheduler {

	private final GetBatchRepository getBatchRepository;
	private final GetBatchSoapService getBatchSoapService;
	private final int maxJobsPerRun;
	private final int soapPageSize;

	public GetBatchScheduler(GetBatchRepository getBatchRepository,
							 GetBatchSoapService getBatchSoapService,
							 @Value("${payment-export.scheduler.get-batch.max-jobs-per-run:10}") int maxJobsPerRun,
							 @Value("${payment-export.scheduler.get-batch.page-size:100}") int soapPageSize) {
		this.getBatchRepository = getBatchRepository;
		this.getBatchSoapService = getBatchSoapService;
		this.maxJobsPerRun = Math.max(1, maxJobsPerRun);
		this.soapPageSize = Math.max(1, soapPageSize);
	}

	@Scheduled(fixedDelayString = "${payment-export.scheduler.get-batch.fixed-delay:60000}")
	public void fetchBatches() {
		for (GetBatchJob batchJob : getBatchRepository.findCreatedJobsForBatchFetch(maxJobsPerRun, soapPageSize)) {
			processJob(batchJob);
		}
	}

	private void processJob(GetBatchJob batchJob) {
		GetBatchRequest initialRequest = batchJob.request();
		var jobId = batchJob.jobId();
		try {
			getBatchRepository.markJobAsFetchingBatches(jobId);

			GetBatchRequest currentRequest = initialRequest;
			boolean moreResultsAvailable;
			do {
				GetBatchResponse response = getBatchSoapService.call(currentRequest);
				getBatchRepository.saveBatchPage(jobId, response);

				moreResultsAvailable = response.moreResultsAvailable();
				currentRequest = currentRequest.nextPage();
			} while (moreResultsAvailable);

			getBatchRepository.markJobAsBatchesFetched(jobId);
		} catch (Exception exception) {
			log.error("Failed to fetch batches for job {}", jobId, exception);
			getBatchRepository.markJobAsFailed(jobId, exception.getMessage());
		}
	}
}
