package ji.hs.firedct.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ji.hs.firedct.itm.svc.ItmService;
import ji.hs.firedct.itmtrd.svc.ItmTrdService;

@Configuration
@EnableScheduling
public class SchedulerConfig {
	@Autowired
	private ItmService itmService;
	
	@Autowired
	private ItmTrdService itmTrdService;
	
	@Scheduled(cron = "0 20 * * * *")
	public void itmCrawling() {
		itmService.itmCrawling();
	}
	
	
	@Scheduled(cron = "0 */5 * * * *")
	public void tmpDt() {
		itmTrdService.tmpDt();
	}
}
