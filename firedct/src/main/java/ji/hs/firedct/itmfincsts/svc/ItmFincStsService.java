package ji.hs.firedct.itmfincsts.svc;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.co.Constant;
import ji.hs.firedct.itm.dao.Itm;
import ji.hs.firedct.itm.dao.ItmRepository;
import ji.hs.firedct.itmfincsts.dao.ItmFincSts;
import ji.hs.firedct.itmfincsts.dao.ItmFincStsRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 종목 재무제표 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class ItmFincStsService {
	@Autowired
	private ItmRepository itmRepo;
	
	@Autowired
	private ItmFincStsRepository itmFincStsRepo;
	
	@Value("${constant.dart.api.key}")
	private String dartApiKey;
	
	/**
	 * Dart 년도 / 분기 재무제표 수집
	 */
	public void itmFincStsCrawling(final String yr, final String qt) {
		log.info("{}년도 {} 재무제표 수집 시작", yr, qt);
		
		final ObjectMapper mapper = new ObjectMapper();
		
		// Dart 종목 코드를 가진 모든 자료 조회
		final List<Itm> itmLst = itmRepo.findByDartItmCdIsNotNull(null);
		
		// 종목 재무제표 List
		final List<ItmFincSts> itmFincStsLst = new ArrayList<ItmFincSts>();
		
		itmLst.stream().forEach(itm -> {
			try{
				Map<String, Object> map = mapper.readValue(getDataFromUrl(itm.getDartItmCd(), yr, qt), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
				
				if("000".equals(map.get("status"))) {
					
					List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("list");
					
					ItmFincSts itmFincSts = new ItmFincSts();
					
					itmFincSts.setItmCd(itm.getItmCd());
					itmFincSts.setMkt(itm.getMkt());
					itmFincSts.setYr(yr);
					itmFincSts.setQt(qt);
					
					blockLst.stream().forEach(block -> {
						if("ifrs-full_Revenue".equals(block.get("account_id"))) {
							if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
								itmFincSts.setSalAmt(new BigDecimal(block.get("thstrm_amount")));
							}
						}else if("dart_OperatingIncomeLoss".equals(block.get("account_id"))) {
							if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
								itmFincSts.setOprIncmAmt(new BigDecimal(block.get("thstrm_amount")));
							}
						}else if("ifrs-full_ProfitLoss".equals(block.get("account_id"))) {
							if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
								itmFincSts.setTsNetIncmAmt(new BigDecimal(block.get("thstrm_amount")));
							}
						}
					});
					
					log.info("{}, {}, {}, {}, {}", itmFincSts.getItmCd(), itmFincSts.getMkt(), itmFincSts.getSalAmt(), itmFincSts.getOprIncmAmt(), itmFincSts.getTsNetIncmAmt());
					
					itmFincStsLst.add(itmFincSts);
				}
				
			}catch(Exception e) {
				log.error("", e);
			}
		});
		
		itmFincStsRepo.saveAllAndFlush(itmFincStsLst);
		
		log.info("{}년도 {} 재무제표 수집 종료", yr, qt);
	}
	
	/**
	 * 
	 * @param itmCd
	 * @param yr
	 * @param qt
	 * @return
	 */
	private String getDataFromUrl(final String itmCd, final String yr, final String qt) {
		String result = null;
		InputStream is = null;
		try {
			// 1분에 1000건이 넘으면 24시간 차단을 당하기 때문에 100ms 슬립한다.
			Thread.sleep(100L);
			
			URL url = new URL(Constant.DART_FINC_STS_URL + "?crtfc_key=" + dartApiKey + "&corp_code=" + itmCd + "&bsns_year=" + yr + "&reprt_code=" + qt + "&fs_div=CFS");
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
