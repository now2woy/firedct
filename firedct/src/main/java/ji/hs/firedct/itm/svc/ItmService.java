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

import ji.hs.firedct.itm.dao.Itm;
import ji.hs.firedct.itm.dao.ItmRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ItmService {
	@Autowired
	private ItmRepository itmRepo;
	
	public void itmCrawling() {
		final String URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd?bld=dbms/MDC/STAT/standard/MDCSTAT01901&mktId=";
		
		final List<String> mktLst = new ArrayList<>();
		final List<Itm> itmLst = new ArrayList<>();
		
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		final ObjectMapper mapper = new ObjectMapper();
		
		mktLst.add("STK");
		mktLst.add("KSQ");
		
		mktLst.stream().forEach(val -> {
			Document doc;
			try {
				log.info("{} 수집 시작", val);
				
				doc = Jsoup.connect(URL + val).get();
				
				Map<String, Object> map = new HashMap<>();
				map = mapper.readValue(doc.text(), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
				
				List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("OutBlock_1");
				
				blockLst.stream().forEach(json -> {
					Itm itm = new Itm();
					
					try {
						itm.setItmCd(json.get("ISU_SRT_CD"));
						itm.setItmNm(json.get("ISU_NM"));
						itm.setMkt("STK".equals(val) ? "00001" : "00002");
						itm.setPubDt(format.parse(json.get("LIST_DD")));
						itm.setStdItmCd(json.get("ISU_CD"));
					}catch (Exception e) {
						log.error("", e);
					}
					
					itmLst.add(itm);
				});
				
				log.info("{} 수집 종료", val);
				
			} catch (IOException e) {
				log.error("", e);
			}
		});
		
		itmRepo.saveAllAndFlush(itmLst);
		
		log.info("{}건 ITM 테이블 데이터 처리 종료", itmLst.size());
	}
}
