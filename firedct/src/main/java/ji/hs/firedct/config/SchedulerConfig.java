package ji.hs.firedct.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ji.hs.firedct.batch.dartfnltt.svc.DartFnlttService;
import ji.hs.firedct.batch.itm.svc.ItmService;
import ji.hs.firedct.batch.itmfincsts.svc.ItmFincStsService;
import ji.hs.firedct.batch.itmtrd.svc.ItmTrdService;
import ji.hs.firedct.batch.tactic.svc.Tactic020Service;
import ji.hs.firedct.batch.tactic.svc.Tactic024Service;
import ji.hs.firedct.co.Utils;
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
	private DartFnlttService dartFnlttService;
	
	@Autowired
	private Tactic020Service tactic020Service;
	
	@Autowired
	private Tactic024Service tactic024Service;
	
	private boolean isRun = false;
	
	/**
	 * 매일 20시 00분
	 * KRX 종목 기본 정보 수집
	 */
	@Scheduled(cron = "0 0 20 * * *")
	public void crawling() {
		// KRX 종목 기본 정보 수집
		itmService.crawling();
		
		// DART 종목코드 수집
		itmService.dartCoprCdFileDownload();
		
		// 최종수집일 + 1부터 현재일까지 KRX 일자별 종목 시세 수집
		itmTrdService.crawling();
	}
	
	/**
	 * 테스트용 스케쥴
	 */
	/*
	@Scheduled(cron = "* * * * * *")
	public void test() {
		try {
			if(!isRun) {
				log.info("시작");
				//itmTrdService.crawling();
				//dartFnlttService.dartCrawling("2019", "2", null);
				
				// Dart 년도 / 분기 재무제표 수집
				//itmFincStsService.crawling("2021", "4", null, true);
				//itmTrdService.createPer("20220415");
				//itmTrdService.createBpsAndPbr(Utils.dateFormat(dt));
				//itmTrdService.sendGoogleSheet("20220408");
				
				//tactic020Service.publishing("20220415");
				//tactic024Service.publishing("20220415");
				
				//Date dt = Utils.dateParse("20210401");
				//boolean isStop = false;
				//
				//while(!isStop) {
				//	itmTrdService.createBPSAndPBRAndSPSAndPSR(Utils.dateFormat(dt));
				//	itmTrdService.createPer(Utils.dateFormat(dt));
				//	
				//	dt = DateUtils.addDays(dt, 1);
				//	
				//	if("20220415".equals(Utils.dateFormat(dt))) {
				//		isStop = true;
				//	}
				//}
				
				log.info("종료");
				isRun = true;
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	*/
}
