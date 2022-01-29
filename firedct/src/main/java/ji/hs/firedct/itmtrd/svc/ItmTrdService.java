package ji.hs.firedct.itmtrd.svc;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.cd.dao.Cd;
import ji.hs.firedct.cd.dao.CdRepository;
import ji.hs.firedct.itmtrd.dao.ItmTrd;
import ji.hs.firedct.itmtrd.dao.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ItmTrdService {
	@Autowired
	private CdRepository cdRepo;
	
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	/**
	 * KRX 종목 시세 수집
	 */
	public void itmTrdCrawling() {
		final String URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd?bld=dbms/MDC/STAT/standard/MDCSTAT01501&mktId={}&trdDd=";
		
		final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		
		final ObjectMapper mapper = new ObjectMapper();
		
		final List<String> dateLst = new ArrayList<>();
		final List<Cd> mktLst = cdRepo.findByCls("00001");
		
		int currDate = Integer.parseInt(format.format(new Date()));
		Date lastCrawlingdate = itmTrdRepo.findMaxDtByItmCd("005930");
		
		while(currDate > Integer.parseInt(format.format(lastCrawlingdate))) {
			lastCrawlingdate = DateUtils.addDays(lastCrawlingdate, 1);
			dateLst.add(format.format(lastCrawlingdate));
			
			log.info("{}", format.format(lastCrawlingdate));
		}
		
		mktLst.stream().forEach(mkt -> {
			log.info("{} 수집 시작", mkt);
			
			dateLst.stream().forEach(date -> {
				try {
					final List<ItmTrd> itmLst = new ArrayList<>();
					
					log.info("{} {} 수집 시작", mkt, date);
					
					Document doc = Jsoup.connect(URL.replace("{}",  mkt.getCd()) + date).get();
					
					Map<String, Object> map = new HashMap<>();
					map = mapper.readValue(doc.text(), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
					
					List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("OutBlock_1");
					
					blockLst.stream().forEach(itm -> {
						
						// 휴일엔 종가가 없다.
						if(!"-".equals(itm.get("TDD_CLSPRC"))) {
							ItmTrd itmTrd = new ItmTrd();
							
							try {
								itmTrd.setItmCd(itm.get("ISU_SRT_CD"));
								itmTrd.setDt(format.parse(date));
								itmTrd.setEdAmt(new BigDecimal(itm.get("TDD_CLSPRC").replaceAll(",", "")));
								itmTrd.setIncr(new BigDecimal(itm.get("CMPPREVDD_PRC").replaceAll(",", "")));
								itmTrd.setTrdQty(new BigDecimal(itm.get("ACC_TRDVOL").replaceAll(",", "")));
								itmTrd.setTrdAmt(new BigDecimal(itm.get("ACC_TRDVAL").replaceAll(",", "")));
								itmTrd.setMktTotAmt(new BigDecimal(itm.get("MKTCAP").replaceAll(",", "")));
								itmTrd.setIsuStkQty(new BigDecimal(itm.get("LIST_SHRS").replaceAll(",", "")));
								
								itmLst.add(itmTrd);
							}catch(Exception e) {
								log.error("", e);
							}
						}
					});
					
					itmTrdRepo.saveAllAndFlush(itmLst);
					
					log.info("{} {} 수집 종료", mkt, date);
				}catch(Exception e) {
					log.error("", e);
				}
			});
			
			log.info("{} 수집 종료", mkt);
		});
	}
}
