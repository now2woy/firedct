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
		
		// KRX 종목 시세 정보 수집
		itmTrdService.itmTrdCrawling();
		
		// 5일 이동 평균 금액 생성
		itmTrdService.createVsttmMvAvgAmt(5000);
		
		// 20일 이동 평균 금액 생성
		itmTrdService.createSttmMvAvgAmt(5000);
		
		// 60일 이동 평균 금액 생성
		itmTrdService.createMdtmMvAvgAmt(5000);
		
		// 120일 이동 평균 금액 생성
		itmTrdService.createLntmMvAvgAmt(5000);
	}
	
	/**
	 * 테스트용 스케쥴
	 */
	@Scheduled(cron = "*/1 * * * * *")
	public void test() {
		
		if(pgrLokService.getLokYn("00001")) {
			log.info("프로그램 실행 시작");
			
			try {
				Thread.sleep(20000L);
			}catch(Exception e) {
				log.error("", e);
			}
			
			pgrLokService.unLok("00001");
			log.info("프로그램 실행 종료");
		}else {
			log.info("프로그램 잠금 중");
		}
	}
}
