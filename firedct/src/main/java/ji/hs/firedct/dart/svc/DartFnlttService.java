package ji.hs.firedct.dart.svc;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.data.dart.dartfnltt.entity.DartFnltt;
import ji.hs.firedct.data.dart.dartfnltt.repository.DartFnlttRepository;
import ji.hs.firedct.data.stock.cd.repository.CdRepository;
import ji.hs.firedct.data.stock.itm.entity.Itm;
import ji.hs.firedct.data.stock.itm.repository.ItmRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * DART 임시 재무제표 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class DartFnlttService {
	@Autowired
	private CdRepository cdRepo;
	
	@Autowired
	private ItmRepository itmRepo;
	
	@Autowired
	private DartFnlttRepository dartFnlttRepo;
	
	/**
	 * DART API를 사용하기 위한 KEY
	 */
	@Value("${constant.dart.api.key}")
	private String dartApiKey;
	
	/**
	 * DART 종목 / 년도 / 분기 재무제표 URL
	 */
	@Value("${constant.url.dartFincSts}")
	private String dartFincStsUrl;
	
	/**
	 * Dart 년도 / 분기 재무제표 수집
	 */
	public void dartCrawling(final String yr, final String qt, final String itmCd) {
		try {
			log.info("{}년도 {}분기 재무제표 수집 시작", yr, qt);
			
			final ObjectMapper mapper = new ObjectMapper();
			
			// Dart 종목 코드를 가진 모든 자료 조회
			List<Itm> itms = null;
			
			// 종목코드가 있을 경우
			if(StringUtils.isNotEmpty(itmCd)) {
				Optional<Itm> temp = itmRepo.findByItmCd(itmCd);
				
				// 조회 결과가 있을 경우
				if(temp.isPresent()) {
					// 대상 리스트에 담는다.
					itms = new ArrayList<>();
					itms.add(temp.get());
				}
				
				// 종목코드가 없을 경우
			}else {
				// 전체 리스트를 조회한다.
				itms = itmRepo.findByDartItmCdIsNotNull(null);
			}
			
			final String qtCd = cdRepo.findCdByClsAndCdSubNm("00004", qt);
			
			// 대상 목록 루핑
			itms.stream().forEach(itm -> {
				// 종목 재무제표 List
				final List<DartFnltt> dartFnltts = new ArrayList<>();
				
				try{
					Map<String, Object> map = mapper.readValue(getDataFromUrl(itm.getDartItmCd(), yr, qtCd, "CFS"), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
					
					if("013".equals(map.get("status"))) {
						map = mapper.readValue(getDataFromUrl(itm.getDartItmCd(), yr, qtCd, "OFS"), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
						
					// 사용한도를 초과
					}else if("020".equals(map.get("status"))) {
						log.info("{}, {}", "사용한도를 초과하였습니다.", itm.toString());
					}
					
					// URL 조회 결과가 정상일 경우
					if("000".equals(map.get("status"))) {
						AtomicLong seq = new AtomicLong();
						
						List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("list");
						
						blockLst.stream().forEach(block -> {
							DartFnltt dartFnltt = new DartFnltt();
							
							dartFnltt.setItmCd(itm.getItmCd());
							dartFnltt.setMkt(itm.getMkt());
							dartFnltt.setYr(yr);
							dartFnltt.setQt(qt);
							dartFnltt.setSeq(seq.getAndIncrement());
							
							if(block.containsKey("reprt_code")) {
								dartFnltt.setReprtCd(block.get("reprt_code"));
							}
							if(block.containsKey("sj_div")) {
								dartFnltt.setSjDiv(block.get("sj_div"));
							}
							if(block.containsKey("sj_nm")) {
								dartFnltt.setSjNm(block.get("sj_nm"));
							}
							if(block.containsKey("account_id")) {
								dartFnltt.setAcntId(block.get("account_id"));
							}
							if(block.containsKey("account_nm")) {
								dartFnltt.setAcntNm(block.get("account_nm"));
							}
							if(block.containsKey("account_detail")) {
								dartFnltt.setAcntDtl(block.get("account_detail"));
							}
							if(block.containsKey("thstrm_nm")) {
								dartFnltt.setThTmNm(block.get("thstrm_nm"));
							}
							if(block.containsKey("thstrm_amount")) {
								dartFnltt.setThTmAmt(block.get("thstrm_amount"));
							}
							if(block.containsKey("thstrm_add_amount")) {
								dartFnltt.setThTmAddAmt(block.get("thstrm_add_amount"));
							}
							if(block.containsKey("frmtrm_nm")) {
								dartFnltt.setFrmTmNm(block.get("frmtrm_nm"));
							}
							if(block.containsKey("frmtrm_q_nm")) {
								dartFnltt.setFrmTmNm(block.get("frmtrm_q_nm"));
							}
							if(block.containsKey("frmtrm_q_amount")) {
								dartFnltt.setFrmTmAmt(block.get("frmtrm_q_amount"));
							}
							if(block.containsKey("frmtrm_add_amount")) {
								dartFnltt.setFrmTmAddAmt(block.get("frmtrm_add_amount"));
							}
							if(block.containsKey("reprt_code")) {
								dartFnltt.setOrd(block.get("ord"));
							}
							
							dartFnltts.add(dartFnltt);
						});
					}
					
					dartFnlttRepo.saveAllAndFlush(dartFnltts);
					
					log.info("{}", itm.toString());
					
				}catch(Exception e) {
					log.error("", e);
				}
			});
			
			log.info("{}년도 {}분기 재무제표 수집 종료", yr, qt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 
	 * @param itmCd
	 * @param yr
	 * @param qt
	 * @return
	 */
	private String getDataFromUrl(final String itmCd, final String yr, final String qt, String fsDiv) {
		String result = null;
		InputStream is = null;
		try {
			// 1분에 1000건이 넘으면 24시간 차단을 당하기 때문에 100ms 슬립한다.
			Thread.sleep(100L);
			
			URL url = new URL(dartFincStsUrl + "?crtfc_key=" + dartApiKey + "&corp_code=" + itmCd + "&bsns_year=" + yr + "&reprt_code=" + qt + "&fs_div=" + fsDiv);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			is = conn.getInputStream();
			
			result = IOUtils.toString(is, "UTF-8");
		}catch(Exception e) {
			log.error("", e);
		}finally {
			IOUtils.closeQuietly(is);
		}
		
		return result;
	}
}
