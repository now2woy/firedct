package ji.hs.firedct.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ji.hs.firedct.itm.svc.ItmFincStsService;
import ji.hs.firedct.itm.svc.ItmService;
import ji.hs.firedct.itm.svc.ItmTrdService;
import ji.hs.firedct.pgr.svc.PgrLokService;
import lombok.extern.slf4j.Slf4j;

/**
 * Schedule 설정
 * @author now2woy
 *
 */
@Slf4j
@Configuration
@EnableScheduling
public class SchedulerConfig {
	@Autowired
	private ItmService itmService;
	
	@Autowired
	private ItmTrdService itmTrdService;
	
	@Autowired
	private ItmFincStsService itmFincStsService;
	
	@Autowired
	private PgrLokService pgrLokService;
	
	/**
	 * 매일 20시 00분
	 * KRX 종목 기본 정보 수집
	 */
	@Scheduled(cron = "0 0 20 * * *")
	public void crawling() {
		// KRX 종목 기본 정보 수집
		itmService.itmCrawling();
		
		// DART 종목코드 수집
		itmService.dartCoprCdFileDownload();
		
		// 최종수집일 + 1부터 현재일까지 KRX 일자별 종목 시세 수집
		itmTrdService.itmTrdCrawling();
	}
	
	/**
	 * 테스트용 스케쥴
	 */
	/*
	@Scheduled(cron = "0 1/5 * * * *")
	public void test() {
		itmFincStsService.itmFincStsCrawling("2021", "11014", "009540");
	}
	*/
}
