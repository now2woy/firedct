package ji.hs.firedct.itm.svc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
	 * 
	 * @return
	 */
	public List<Itm> getAllItms(){
		return itmRepo.findAll(PageRequest.of(0, 100, Sort.by("itmCd").ascending())).toList();
	}
	
	/**
	 * KRX 종목 기본 정보 수집
	 */
	public void itmCrawling() {
		final List<Cd> mktLst = cdRepo.findByCls("00001");
		final List<Itm> itmLst = new ArrayList<>();
		
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		final ObjectMapper mapper = new ObjectMapper();
		
		mktLst.stream().forEach(mkt -> {
			try {
				log.info("{} 수집 시작", mkt.getCd());
				
				Document doc = Jsoup.connect(Constant.KRX_JSON_URL)
								.data("bld", "dbms/MDC/STAT/standard/MDCSTAT01901")
								.data("mktId", mkt.getCd())
								.get();
				
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
			URL url = new URL(Constant.DART_CORP_CD_URL + "?crtfc_key=" + dartApiKey);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			is = conn.getInputStream();
			
			unzip(is, new File(Constant.ENV_DOWNLOAD_PATH), "UTF-8");
			
			dartCoprCdParser();
			
		}catch(Exception e) {
			log.error("", e);
		}finally {
			IOUtils.closeQuietly(is);
		}
		log.info("Dart 종목코드 파일 다운로드 종료");
	}
	
	/**
	 * 압축을 푼다.
	 * @param is
	 * @param destDir
	 * @param charsetName
	 * @throws IOException
	 */
	private void unzip(InputStream is, File destDir, String charsetName) throws IOException {
		ZipArchiveInputStream zis = null;
		ZipArchiveEntry entry = null;
		String name = null;
		File target = null;
		FileOutputStream fos = null;
		
		try {
			zis = new ZipArchiveInputStream(is, charsetName, false);
			
			while ((entry = zis.getNextZipEntry()) != null){
				name = entry.getName();
				
				target = new File (destDir, name);
				
				if(entry.isDirectory()){
					target.mkdirs();
				} else {
					target.createNewFile();
					
					try {
						fos = new FileOutputStream(target);
						IOUtils.copy(zis, fos);
					}catch(Exception e) {
						log.error("", e);
					}finally {
						IOUtils.closeQuietly(fos);
					}
				}
			}
		}catch(Exception e) {
			log.error("", e);
		}finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(fos);
		}
	}
	
	/**
	 * Dart 종목코드 파일 수집
	 */
	private void dartCoprCdParser() {
		log.info("Dart 종목코드 파일 수집 시작");
		
		try {
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(Constant.ENV_DOWNLOAD_PATH + "/CORPCODE.xml"));
			
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
