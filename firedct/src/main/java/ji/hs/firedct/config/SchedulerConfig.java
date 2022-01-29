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
	
	/**
	 * (매일 20시 00분)KRX 종목 기본 정보 수집
	 */
	@Scheduled(cron = "0 0 20 * * *")
	public void itmCrawling() {
		itmService.itmCrawling();
	}
	
	/**
	 * (매일 20시 30분)KRX 종목 시세 정보 수집
	 */
	@Scheduled(cron = "0 30 20 * * *")
	public void tmpDt() {
		itmTrdService.itmTrdCrawling();
	}
}
