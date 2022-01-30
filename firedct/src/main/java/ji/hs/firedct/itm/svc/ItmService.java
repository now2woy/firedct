package ji.hs.firedct.itm.svc;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.cd.dao.Cd;
import ji.hs.firedct.cd.dao.CdRepository;
import ji.hs.firedct.co.Constant;
import ji.hs.firedct.itm.dao.Itm;
import ji.hs.firedct.itm.dao.ItmRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 종목 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class ItmService {
	@Autowired
	private CdRepository cdRepo;
	
	@Autowired
	private ItmRepository itmRepo;
	
	@Value("${constant.dart.api.key}")
	private String dartApiKey;
	
	/**
	 * KRX 종목 기본 정보 수집
	 */
	public void itmCrawling() {
		final String URL = Constant.KRX_JSON_URL + "?bld=dbms/MDC/STAT/standard/MDCSTAT01901&mktId=";
		
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
	
	public void dartCoprCdFileDownload() {
		final String URL = "";
	}
	
	/**
	 * Dart 종목코드 파일 수집
	 */
	public void dartCoprCdParser() {
		log.info("Dart 종목코드 파일 수집 시작");
		
		try {
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream("/Downloads/CORPCODE.xml"));
			
			List<Itm> itmLst = new ArrayList<>();
			Itm itm = null;
			
			while (reader.hasNext()) {
				XMLEvent nextEvent = reader.nextEvent();
				
				if (nextEvent.isStartElement()) {
					StartElement startElement = nextEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("list")) {
						itm = new Itm();
					}else if(startElement.getName().getLocalPart().equals("corp_code")) {
						nextEvent = reader.nextEvent();
						itm.setDartItmCd(nextEvent.asCharacters().getData());
					}else if(startElement.getName().getLocalPart().equals("stock_code")) {
						nextEvent = reader.nextEvent();
						itm.setItmCd(nextEvent.asCharacters().getData().trim());
					}
				}
				
				if (nextEvent.isEndElement()) {
					EndElement endElement = nextEvent.asEndElement();
					
					if (endElement.getName().getLocalPart().equals("list")) {
						if(StringUtils.isNotEmpty(itm.getItmCd())) {
							Optional<Itm> tmp = itmRepo.findByItmCd(itm.getItmCd());
							
							if(tmp.isPresent()) {
								itm.setItmNm(tmp.get().getItmNm());
								itm.setMkt(tmp.get().getMkt());
								itm.setPubDt(tmp.get().getPubDt());
								itm.setStdItmCd(tmp.get().getStdItmCd());
								
								itmLst.add(itm);
							}
						}
					}
				}
			}
			
			itmRepo.saveAllAndFlush(itmLst);
			
		}catch(Exception e) {
			log.error("", e);
		}
		
		log.info("Dart 종목코드 파일 수집 종료");
	}
}
