package ji.hs.firedct.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.dart.svc.DartFnlttService;
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
	private DartFnlttService dartFnlttService;
	
	@Autowired
	private PgrLokService pgrLokService;
	
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
				//dartFnlttService.dartCrawling("2021", "1", null);
				//itmFincStsService.crawling("2021", "4", null, true);
				//itmTrdService.createPer("20210401");
				//itmTrdService.createBpsAndPbr(Utils.dateFormat(dt));
				//itmTrdService.sendGoogleSheet("20220408");
				//itmTrdService.tactic20("20220408");
				
				Date dt = Utils.dateParse("20210401");
				boolean isStop = false;
				
				while(!isStop) {
					itmTrdService.createBpsAndPbr(Utils.dateFormat(dt));
					itmTrdService.createPer(Utils.dateFormat(dt));
					
					dt = DateUtils.addDays(dt, 1);
					
					if("20220412".equals(Utils.dateFormat(dt))) {
						isStop = true;
					}
				}
				
				isRun = true;
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	*/
}
