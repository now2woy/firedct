package ji.hs.firedct.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ji.hs.firedct.itm.svc.ItmService;

@Configuration
@EnableScheduling
public class SchedulerConfig {
	@Autowired
	private ItmService itmService;
	
	@Scheduled(cron = "0 20 * * * *")
	public void itmCrawling() {
		itmService.itmCrawling();
	}
}
