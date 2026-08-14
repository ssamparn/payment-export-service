package com.payment.export.platform.application.scheduler;

import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.response.GetBatchResponse;
import com.payment.export.platform.domain.ports.output.repository.GetBatchJobRepository;
import com.payment.export.platform.domain.ports.output.soap.GetBatchSoapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GetBatchScheduler {

	private final GetBatchJobRepository getBatchJobRepository;
	private final GetBatchSoapService getBatchSoapService;
	private final int maxJobsPerRun;
	private final int soapPageSize;

	public GetBatchScheduler(GetBatchJobRepository getBatchJobRepository,
							 GetBatchSoapService getBatchSoapService,
							 @Value("${payment-export.scheduler.get-batch.max-jobs-per-run:10}") int maxJobsPerRun,
							 @Value("${payment-export.scheduler.get-batch.page-size:100}") int soapPageSize) {
		this.getBatchJobRepository = getBatchJobRepository;
		this.getBatchSoapService = getBatchSoapService;
		this.maxJobsPerRun = Math.max(1, maxJobsPerRun);
		this.soapPageSize = Math.max(1, soapPageSize);
	}

	@Scheduled(fixedDelayString = "${payment-export.scheduler.get-batch.fixed-delay:60000}")
	public void fetchBatches() {
		for (GetBatchRequest initialRequest : getBatchJobRepository.findCreatedJobsForBatchFetch(maxJobsPerRun, soapPageSize)) {
			processJob(initialRequest);
		}
	}

	private void processJob(GetBatchRequest initialRequest) {
		try {
			getBatchJobRepository.markJobAsFetchingBatches(initialRequest.jobId());

			GetBatchRequest currentRequest = initialRequest;
			boolean moreResultsAvailable;
			do {
				GetBatchResponse response = getBatchSoapService.call(currentRequest);
				getBatchJobRepository.saveBatchPage(response);

				moreResultsAvailable = response.moreResultsAvailable();
				currentRequest = currentRequest.nextPage();
			} while (moreResultsAvailable);

			getBatchJobRepository.markJobAsBatchesFetched(initialRequest.jobId());
		} catch (Exception exception) {
			log.error("Failed to fetch batches for job {}", initialRequest.jobId(), exception);
			getBatchJobRepository.markJobAsFailed(initialRequest.jobId(), exception.getMessage());
		}
	}
}
