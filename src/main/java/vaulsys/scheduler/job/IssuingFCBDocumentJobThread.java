package vaulsys.scheduler.job;

import vaulsys.scheduler.IssuingFCBDocumentJobInfo;

import java.util.List;

import org.apache.log4j.Logger;

public class IssuingFCBDocumentJobThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());

	List<IssuingFCBDocumentJobInfo> issueJobs;
	
	public IssuingFCBDocumentJobThread(List<IssuingFCBDocumentJobInfo> sortedSettlementData) {
		super();
		this.issueJobs = sortedSettlementData;
	}


	@Override
	public void run() {
		logger.debug("I am here...");
		
		IssuingFCBDocumentJob.processIssueDocumentJobs(issueJobs);
		
		logger.debug("I am exiting....");		
	}
}
