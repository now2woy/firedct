package ji.hs.firedct.itm.svc;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.co.Constant;
import ji.hs.firedct.itm.dao.Itm;
import ji.hs.firedct.itm.dao.ItmFincSts;
import ji.hs.firedct.itm.dao.ItmFincStsRepository;
import ji.hs.firedct.itm.dao.ItmRepository;
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
	public void itmFincStsCrawling(final String yr, final String qt, final String itmCd) {
		log.info("{}년도 {} 재무제표 수집 시작", yr, qt);
		
		final ObjectMapper mapper = new ObjectMapper();
		
		// Dart 종목 코드를 가진 모든 자료 조회
		List<Itm> itmLst = null;
		
		// 종목코드가 있을 경우
		if(StringUtils.isNotEmpty(itmCd)) {
			Optional<Itm> temp = itmRepo.findByItmCd(itmCd);
			
			// 조회 결과가 있을 경우
			if(temp.isPresent()) {
				// 대상 리스트에 담는다.
				itmLst = new ArrayList<>();
				itmLst.add(temp.get());
			}
			
		// 종목코드가 없을 경우
		}else {
			// 전체 리스트를 조회한다.
			itmLst = itmRepo.findByDartItmCdIsNotNull(null);
		}
		
		// 종목 재무제표 List
		final List<ItmFincSts> itmFincStsLst = new ArrayList<ItmFincSts>();
		
		// 대상 목록 루핑
		itmLst.stream().forEach(itm -> {
			try{
				Map<String, Object> map = mapper.readValue(getDataFromUrl(itm.getDartItmCd(), yr, qt), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
				
				// URL 조회 결과가 정상일 경우
				if("000".equals(map.get("status"))) {
					
					List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("list");
					
					ItmFincSts itmFincSts = new ItmFincSts();
					
					itmFincSts.setItmCd(itm.getItmCd());
					itmFincSts.setMkt(itm.getMkt());
					itmFincSts.setYr(yr);
					itmFincSts.setQt(qt);
					
					Map<String, BigDecimal> temp = new HashMap<>();
					
					blockLst.stream().forEach(block -> {
						// 영업수익, 매출액
						if("ifrs-full_GrossProfit".equals(block.get("account_id"))
						|| "ifrs-full_Revenue".equals(block.get("account_id"))){
							if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
								itmFincSts.setSalAmt(new BigDecimal(block.get("thstrm_amount")));
							}
							
						// 영업이익
						}else if("dart_OperatingIncomeLoss".equals(block.get("account_id"))) {
							if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
								itmFincSts.setOprIncmAmt(new BigDecimal(block.get("thstrm_amount")));
							}
							
						// 당기순이익
						}else if("ifrs-full_ProfitLoss".equals(block.get("account_id"))) {
							// 당기순이익의 경우 여러 태그가 있는데 이중 두가지
							if("연결재무제표 [member]".equals(block.get("account_detail"))
							|| "자본 [member]|이익잉여금(결손금)".equals(block.get("account_detail"))
							|| "-".equals(block.get("account_detail"))){
								if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
									itmFincSts.setTsNetIncmAmt(new BigDecimal(block.get("thstrm_amount")));
								}
							}
						
						// 법인세비용차감전순이익(손실)
						}else if("ifrs-full_ProfitLossBeforeTax".equals(block.get("account_id"))) {
							if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
								temp.put("법인세비용차감전순이익", new BigDecimal(block.get("thstrm_amount")));
							}
							
						// 법인세비용(수익)
						}else if("ifrs-full_IncomeTaxExpenseContinuingOperations".equals(block.get("account_id"))) {
							if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
								temp.put("법인세비용", new BigDecimal(block.get("thstrm_amount")));
							}
							
						// 표준계정코드가 아니더라도 필요한 자료일 수 있다
						}else if("-표준계정코드 미사용-".equals(block.get("account_id"))) {
							// 영업수익, 매출액
							if("영업수익".equals(block.get("account_nm"))
							|| "I. 영업수익".equals(block.get("account_nm"))
							|| "매출액".equals(block.get("account_nm"))){
								if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
									itmFincSts.setSalAmt(new BigDecimal(block.get("thstrm_amount")));
								}
								
							// 영업이익
							}else if("영업이익".equals(block.get("account_nm"))) {
								if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
									itmFincSts.setOprIncmAmt(new BigDecimal(block.get("thstrm_amount")));
								}
								
							// 당기순이익
							}else if("당기순이익".equals(block.get("account_nm"))
								  || "당기순이익.".equals(block.get("account_nm"))
								  || "분기순이익".equals(block.get("account_nm"))
								  || "연결당기순이익".equals(block.get("account_nm"))
								  || "연결분기순이익".equals(block.get("account_nm"))
								  || "당기순이익(손실)".equals(block.get("account_nm"))
								  || "분기순이익(손실)".equals(block.get("account_nm"))
								  || "1. 당기순이익(손실)".equals(block.get("account_nm"))
								  || "연결당기순이익(손실)".equals(block.get("account_nm"))
								  || "분기연결순이익(손실)".equals(block.get("account_nm"))) {
								// 당기순이익의 경우 여러 태그가 있는데 이중 두가지
								if("연결재무제표 [member]".equals(block.get("account_detail"))
								|| "-".equals(block.get("account_detail"))){
									if(StringUtils.isNotEmpty(block.get("thstrm_amount"))) {
										itmFincSts.setTsNetIncmAmt(new BigDecimal(block.get("thstrm_amount")));
									}
								}
							}
						}
					});
					
					// 당기순이익이 없고 법인세비용차감전순이익과 법인세비용이 있을 경우 계산한다.
					if(itmFincSts.getTsNetIncmAmt() == null && temp.containsKey("법인세비용차감전순이익") && temp.containsKey("법인세비용")){
						// 당기순이익 = 법인세비용차감전순이익 - 법인세비용
						itmFincSts.setTsNetIncmAmt(temp.get("법인세비용차감전순이익").subtract(temp.get("법인세비용")));
					}
					
					// temp 객체 초기화
					if(temp.containsKey("법인세비용차감전순이익")) {
						temp.remove("법인세비용차감전순이익");
					}
					
					// temp 객체 초기화
					if(temp.containsKey("법인세비용")) {
						temp.remove("법인세비용");
					}
					
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
