package ji.hs.firedct.itmtrd.svc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ji.hs.firedct.itmtrd.dao.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ItmTrdService {
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	public void itmTrdCrawling() {
		final String URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd?bld=dbms/MDC/STAT/standard/MDCSTAT01501&mktId={}&trdDd=";
		
		final List<String> mktLst = new ArrayList<>();
		
		mktLst.add("STK");
		mktLst.add("KSQ");
		
		getCrawligDate();
	}
	
	private List<String> getCrawligDate(){
		List<String> dateLst = new ArrayList<>();
		
		Date currdate = new Date();
		Date lastCrawlingdate = itmTrdRepo.findMaxTmpDtByItmCd("005930");
		
		final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		
		log.info("{}", format.format(currdate));
		log.info("{}", format.format(lastCrawlingdate));
		log.info("{}", currdate.compareTo(lastCrawlingdate));
		
		return dateLst;
	}
}
