package ji.hs.firedct.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ji.hs.firedct.itm.svc.ItmService;
import ji.hs.firedct.itmtrd.svc.ItmTrdService;

/**
 * Schedule 설정
 * @author now2woy
 *
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
	@Autowired
	private ItmService itmService;
	
	@Autowired
	private ItmTrdService itmTrdService;
	
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
		
		// KRX 종목 시세 정보 수집
		itmTrdService.itmTrdCrawling();
	}
	
	/**
	 * 테스트용 스케쥴
	 */
	@Scheduled(cron = "0 */1 * * * *")
	public void test() {
		// 5일 이동 평균 금액 생성
		itmTrdService.createVsttmMvAvgAmt(1000);
		
		// 20일 이동 평균 금액 생성
		itmTrdService.createSttmMvAvgAmt(1000);
		
		// 60일 이동 평균 금액 생성
		itmTrdService.createMdtmMvAvgAmt(1000);
		
		// 120일 이동 평균 금액 생성
		itmTrdService.createLntmMvAvgAmt(1000);
	}
}
