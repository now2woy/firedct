package ji.hs.firedct.batch.itm.svc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.Cd;
import ji.hs.firedct.data.stock.entity.Itm;
import ji.hs.firedct.data.stock.repository.CdRepository;
import ji.hs.firedct.data.stock.repository.ItmRepository;
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
	
	/**
	 * DART API를 사용하기 위한 KEY
	 */
	@Value("${constant.dart.api.key}")
	private String dartApiKey;
	
	/**
	 * 거래소에서 JSON 데이터를 가져오는 URL
	 */
	@Value("${constant.url.krxJson}")
	private String krxJsonUrl;
	
	/**
	 * DART에서 종목코드를 가져오는 URL
	 */
	@Value("${constant.url.dartCoprCd}")
	private String dartCoprCdUrl;
	
	/**
	 * DART 종목코드는 압축파일로 다운로드 되어 파일 저장 경로가 필요
	 */
	@Value("${constant.path.download}")
	private String downloadPath;
	
	/**
	 * KRX 종목 기본 정보 수집
	 */
	public void crawling() {
		// 시장 코드 조회
		final List<Cd> mktLst = cdRepo.findByCls("00001");
		// 종목정보를 담을 List 생성
		final List<Itm> itmLst = new ArrayList<>();
		
		mktLst.stream().forEach(mkt -> {
			try {
				log.info("{} 수집 시작", mkt.getCd());
				
				Document doc = Jsoup.connect(krxJsonUrl)
								.data("bld", "dbms/MDC/STAT/standard/MDCSTAT01901")
								.data("mktId", mkt.getCd())
								.get();
				
				Map<String, Object> map = new HashMap<>();
				map = Utils.jsonParse(doc.text());
				
				List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("OutBlock_1");
				
				blockLst.stream().forEach(json -> {
					Itm itm = new Itm();
					
					try {
						itm.setItmCd(json.get("ISU_SRT_CD"));
						itm.setItmNm(json.get("ISU_NM"));
						itm.setMkt(mkt.getCd());
						itm.setPubDt(Utils.dateParse("yyyy/MM/dd", json.get("LIST_DD")));
						itm.setStdItmCd(json.get("ISU_CD"));
					}catch (Exception e) {
						log.error("", e);
					}
					
					itmLst.add(itm);
				});
				
				log.info("{} 수집 종료", mkt.getCd());
				
			} catch (IOException e) {
				log.error("", e);
			}
		});
		
		itmRepo.saveAllAndFlush(itmLst);
		
		log.info("{}건 ITM 처리 완료", itmLst.size());
	}
	
	/**
	 * Dart 종목코드 파일 다운로드
	 */
	public void dartCoprCdFileDownload() {
		log.info("Dart 종목코드 파일 다운로드 시작");
		InputStream is = null;
		
		try {
			URL url = new URL(dartCoprCdUrl + "?crtfc_key=" + dartApiKey);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			is = conn.getInputStream();
			
			Utils.unzip(is, new File(downloadPath), "UTF-8");
			
			dartCoprCdParser();
			
		}catch(Exception e) {
			log.error("", e);
		}finally {
			IOUtils.closeQuietly(is);
		}
		log.info("Dart 종목코드 파일 다운로드 종료");
	}
	
	/**
	 * Dart 종목코드 파일 수집
	 */
	private void dartCoprCdParser() {
		log.info("Dart 종목코드 파일 수집 시작");
		
		try {
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(downloadPath + "/CORPCODE.xml"));
			
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