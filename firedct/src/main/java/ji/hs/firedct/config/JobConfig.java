package ji.hs.firedct.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Job 설정
 * @author now2woy
 *
 */
@Configuration
@EnableBatchProcessing
public class JobConfig {
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	/*
	@Autowired
	private CdService cdService;
	
	public Job cdJob() {
		return jobBuilderFactory
				.get("cdJob")
				.start(stepBuilderFactory
						.get("cdStep")
						.<Cd, Cd>chunk(10)
						.reader(null)
						.writer(null)
						.build())
				.build();
	}
	*/
}
