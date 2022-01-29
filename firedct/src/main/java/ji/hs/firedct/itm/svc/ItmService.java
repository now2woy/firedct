package ji.hs.firedct.itm.svc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.cd.dao.Cd;
import ji.hs.firedct.cd.dao.CdRepository;
import ji.hs.firedct.itm.dao.Itm;
import ji.hs.firedct.itm.dao.ItmRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ItmService {
	@Autowired
	private CdRepository cdRepo;
	
	@Autowired
	private ItmRepository itmRepo;
	
	/**
	 * KRX 종목 기본 정보 수집
	 */
	public void itmCrawling() {
		final String URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd?bld=dbms/MDC/STAT/standard/MDCSTAT01901&mktId=";
		
		final List<Cd> mktLst = cdRepo.findByCls("00001");
		final List<Itm> itmLst = new ArrayList<>();
		
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		final ObjectMapper mapper = new ObjectMapper();
		
		mktLst.stream().forEach(mkt -> {
			Document doc;
			try {
				log.info("{} 수집 시작", mkt);
				
				doc = Jsoup.connect(URL + mkt.getCd()).get();
				
				Map<String, Object> map = new HashMap<>();
				map = mapper.readValue(doc.text(), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
				
				List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("OutBlock_1");
				
				blockLst.stream().forEach(json -> {
					Itm itm = new Itm();
					
					try {
						itm.setItmCd(json.get("ISU_SRT_CD"));
						itm.setItmNm(json.get("ISU_NM"));
						itm.setMkt(mkt.getCd());
						itm.setPubDt(format.parse(json.get("LIST_DD")));
						itm.setStdItmCd(json.get("ISU_CD"));
					}catch (Exception e) {
						log.error("", e);
					}
					
					itmLst.add(itm);
				});
				
				log.info("{} 수집 종료", mkt);
				
			} catch (IOException e) {
				log.error("", e);
			}
		});
		
		itmRepo.saveAllAndFlush(itmLst);
		
		log.info("{}건 ITM 처리 완료", itmLst.size());
	}
}
