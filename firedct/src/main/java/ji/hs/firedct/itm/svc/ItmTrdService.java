package ji.hs.firedct.itm.svc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.cd.dao.Cd;
import ji.hs.firedct.cd.dao.CdRepository;
import ji.hs.firedct.co.Constant;
import ji.hs.firedct.itm.dao.ItmRepository;
import ji.hs.firedct.itm.dao.ItmTrd;
import ji.hs.firedct.itm.dao.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 종목 거래 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class ItmTrdService {
	@Autowired
	private CdRepository cdRepo;
	
	@Autowired
	private ItmRepository itmRepo;
	
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	/**
	 * V
	 * 수집된 최종일자의 시총금액 작은순으로 정렬하여 100건 조회
	 * 
	 * @param page
	 * @return
	 */
	public List<ItmTrd> allItmTrd(int page){
		Date maxDt = itmTrdRepo.findMaxDt();
		return itmTrdRepo.findByDt(maxDt, PageRequest.of(page, 100, Sort.by("mktTotAmt").ascending()));
	}
	
	/**
	 * S
	 * 
	 * 최종수집일 + 1부터 현재일까지 KRX 일자별 종목 시세 수집
	 * 매일 수집하는 스케쥴에서 사용
	 */
	public void itmTrdCrawling() {
		try {
			final List<String> dtLst = findMaxDtByItmCd(new SimpleDateFormat("yyyyMMdd"));
			
			dtLst.stream().forEach(dt -> {
				itmTrdCrawlingByDt(dt);
			});
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * S
	 * 
	 * 최종 수집일 다음날 KRX 일자별 종목 시세 수집
	 * 2007년 1월 2일 부터 현재일까지 수집할때 사용
	 */
	public void itmTrdCrawlingByLastDayAfter() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Page<ItmTrd> itmTrd = itmTrdRepo.findAll(PageRequest.of(0, 1, Sort.by("dt").descending()));
		
		// 조회된 데이터가 없을 경우 2007년 1월 2일 부터 수집
		if(itmTrd.isEmpty()) {
			itmTrdCrawlingByDt("20070102");
		}else {
			// 현재일
			final int currDate = Integer.parseInt(format.format(new Date()));
			
			// 가져온 날자 + 1
			Date findDt = DateUtils.addDays(itmTrd.toList().get(0).getDt(), 1);
			
			while(true) {
				// 현재일보다 작거나 같을 경우
				if(currDate >= Integer.parseInt(format.format(findDt))) {
					// 거래내역이 있는지 확인
					if(!isTrdDt(format.format(findDt))) {
						// 거래내역이 없을 경우 증가
						findDt = DateUtils.addDays(findDt, 1);
						
					// 거래내역이 있을 경우 종료
					}else {
						break;
					}
					
				// 현재일보다 클 경우 종료
				}else {
					break;
				}
			}
			
			// 현재일보다 작거나 같을 경우에만 수집
			if(currDate >= Integer.parseInt(format.format(findDt))) {
				itmTrdCrawlingByDt(format.format(findDt));
			}
		}
	}
	
	/**
	 * S
	 * 
	 * 일자에 거래내역이 있는지 확인
	 * @param dt
	 * @return
	 */
	private boolean isTrdDt(final String dt) {
		final ObjectMapper mapper = new ObjectMapper();
		final String mkt = "STK";
		boolean isTrdDt = false;
		
		try {
			Document doc = Jsoup.connect(Constant.KRX_JSON_URL)
							.data("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
							.data("mktId", mkt)
							.data("trdDd", dt)
							.get();
			
			Map<String, Object> map = mapper.readValue(doc.text(), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
			
			List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("OutBlock_1");
			
			if(!"-".equals(blockLst.get(0).get("TDD_CLSPRC"))) {
				isTrdDt = true;
			}
			
		}catch(Exception e) {
			log.error("", e);
		}
		
		return isTrdDt;
	}
	
	/**
	 * S
	 * 
	 * KRX 일자별 종목 시세 수집
	 * @param dt
	 */
	private void itmTrdCrawlingByDt(final String dt) {
		final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		final ObjectMapper mapper = new ObjectMapper();
		final List<Cd> mktLst = cdRepo.findByCls("00001");
		
		mktLst.stream().forEach(mkt -> {
			log.info("{}일 {} 수집 시작", dt, mkt.getCd());
			
			try {
				final List<ItmTrd> itmLst = new ArrayList<>();
				
				Document doc = Jsoup.connect(Constant.KRX_JSON_URL)
								.data("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
								.data("mktId", mkt.getCd())
								.data("trdDd", dt)
								.get();
				
				Map<String, Object> map = mapper.readValue(doc.text(), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
				
				List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("OutBlock_1");
				
				blockLst.stream().forEach(itm -> {
					
					// 휴일엔 종가가 없다.
					if(!"-".equals(itm.get("TDD_CLSPRC"))) {
						ItmTrd itmTrd = new ItmTrd();
						
						try {
							if(itmRepo.findByItmCd(itm.get("ISU_SRT_CD")).isPresent()) {
								itmTrd.setItmCd(itm.get("ISU_SRT_CD"));
								itmTrd.setDt(format.parse(dt));
								itmTrd.setEdAmt(new BigDecimal(itm.get("TDD_CLSPRC").replaceAll(",", "")));
								itmTrd.setIncr(new BigDecimal(itm.get("CMPPREVDD_PRC").replaceAll(",", "")));
								itmTrd.setTrdQty(new BigDecimal(itm.get("ACC_TRDVOL").replaceAll(",", "")));
								itmTrd.setTrdAmt(new BigDecimal(itm.get("ACC_TRDVAL").replaceAll(",", "")));
								itmTrd.setMktTotAmt(new BigDecimal(itm.get("MKTCAP").replaceAll(",", "")));
								itmTrd.setIsuStkQty(new BigDecimal(itm.get("LIST_SHRS").replaceAll(",", "")));
								
								if(itmTrd.getTrdQty().compareTo(BigDecimal.ZERO) > 0 && itmTrd.getIsuStkQty().compareTo(BigDecimal.ZERO) > 0) {
									// ROUND((당일거래수 / 전체주식수) * 100, 2)
									itmTrd.setTrdTnovRt(itmTrd.getTrdQty().divide(itmTrd.getIsuStkQty(), 10, RoundingMode.HALF_EVEN).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_EVEN));
								}else {
									itmTrd.setTrdTnovRt(BigDecimal.ZERO);
								}
								
								itmLst.add(itmTrd);
							}
						}catch(Exception e) {
							log.error("", e);
						}
					}
				});
				
				// 현재 자료를 입력 한다.
				itmTrdRepo.saveAllAndFlush(itmLst);
				
				// DB에 입력 되야 처리 가능한 부분으로 분리가 필요
				if(!itmLst.isEmpty()) {
					itmLst.stream().forEach(itm -> {
						itm.setVsttmMvAvgAmt(createMvAvgAmt(itm, 5));
						itm.setSttmMvAvgAmt(createMvAvgAmt(itm, 20));
						itm.setMdtmMvAvgAmt(createMvAvgAmt(itm, 60));
						itm.setLntmMvAvgAmt(createMvAvgAmt(itm, 120));
						
					});
					
					// 이동평균금액을 저장한다.
					itmTrdRepo.saveAllAndFlush(itmLst);
				}
			}catch(Exception e) {
				log.error("", e);
			}
			
			log.info("{}일 {} 수집 종료", dt, mkt.getCd());
		});
	}
	
	/**
	 * S
	 * 
	 * 최종거래일자부터 현재일자까지 List
	 * 
	 * @param format
	 * @param itmCd
	 * @return
	 */
	private List<String> findMaxDtByItmCd(SimpleDateFormat format) throws Exception {
		final List<String> dateLst = new ArrayList<>();
		final int currDate = Integer.parseInt(format.format(new Date()));
		
		// 최종거래일 조회
		Page<ItmTrd> itmTrd = itmTrdRepo.findAll(PageRequest.of(0, 1, Sort.by("dt").descending()));
		
		Date lastCrawlingdate = null;
		
		// 조회된 데이터가 없을 경우 2007년 1월 2일 부터 수집
		if(itmTrd.isEmpty()) {
			lastCrawlingdate = format.parse("20070102");
			
		// 최종거래일이 있을 경우 사용
		}else {
			lastCrawlingdate = itmTrd.toList().get(0).getDt();
		}
		
		// 현재 일자보다 최종거래일이 작을 경우
		while(currDate > Integer.parseInt(format.format(lastCrawlingdate))) {
			// 최종거래일 + 1 하여 목록에 담는다.
			lastCrawlingdate = DateUtils.addDays(lastCrawlingdate, 1);
			dateLst.add(format.format(lastCrawlingdate));
			
			log.info("{}", format.format(lastCrawlingdate));
		}
		
		return dateLst;
	}
	
	/**
	 * S
	 * 
	 * scale 만큼 평균을 구한다.
	 * 
	 * @param itmTrd
	 * @param scale
	 * @return
	 */
	private BigDecimal createMvAvgAmt(ItmTrd itmTrd, int scale) {
		BigDecimal mvAvgAmt = BigDecimal.ZERO;
		
		List<ItmTrd> itmTrdLst =  itmTrdRepo.findByItmCdAndDtLessThanEqualOrderByDtDesc(itmTrd.getItmCd(), itmTrd.getDt(), PageRequest.of(0, scale));
		
		if(itmTrdLst.size() == scale) {
			for(int i = 0; i < scale; i++) {
				mvAvgAmt = mvAvgAmt.add(itmTrdLst.get(i).getEdAmt());
			}
			
			mvAvgAmt = mvAvgAmt.divide(new BigDecimal(scale), 0, RoundingMode.HALF_EVEN);
		}
		
		return mvAvgAmt;
	}
}
