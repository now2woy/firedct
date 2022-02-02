package ji.hs.firedct.itmtrd.svc;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.cd.dao.Cd;
import ji.hs.firedct.cd.dao.CdRepository;
import ji.hs.firedct.co.Constant;
import ji.hs.firedct.itmtrd.dao.ItmTrd;
import ji.hs.firedct.itmtrd.dao.ItmTrdRepository;
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
	private ItmTrdRepository itmTrdRepo;
	
	/**
	 * KRX 일자별 종목 시세 수집
	 */
	public void itmTrdCrawling() {
		final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		final ObjectMapper mapper = new ObjectMapper();
		
		final List<String> dateLst = findMaxDtByItmCd(format, "005930");
		final List<Cd> mktLst = cdRepo.findByCls("00001");
		
		mktLst.stream().forEach(mkt -> {
			log.info("{} 수집 시작", mkt.getCd());
			
			dateLst.stream().forEach(date -> {
				try {
					final List<ItmTrd> itmLst = new ArrayList<>();
					
					log.info("{} {} 수집 시작", mkt.getCd(), date);
					
					Document doc = Jsoup.connect(Constant.KRX_JSON_URL)
									.data("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
									.data("mktId", mkt.getCd())
									.data("trdDd", date)
									.get();
					
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
					
					log.info("{} {} 수집 종료", mkt.getCd(), date);
				}catch(Exception e) {
					log.error("", e);
				}
			});
			
			log.info("{} 수집 종료", mkt.getCd());
		});
	}
	
	/**
	 * 최종거래일자부터 현재일자까지 List
	 * @param format
	 * @param itmCd
	 * @return
	 */
	private List<String> findMaxDtByItmCd(SimpleDateFormat format, String itmCd){
		final List<String> dateLst = new ArrayList<>();
		final int currDate = Integer.parseInt(format.format(new Date()));
		
		Date lastCrawlingdate = itmTrdRepo.findMaxDtByItmCd(itmCd);
		
		while(currDate > Integer.parseInt(format.format(lastCrawlingdate))) {
			lastCrawlingdate = DateUtils.addDays(lastCrawlingdate, 1);
			dateLst.add(format.format(lastCrawlingdate));
			
			log.info("{}", format.format(lastCrawlingdate));
		}
		
		return dateLst;
	}
	
	/**
	 * 초단기 이동평균금액(5일 이동평균금액) 생성
	 * @param pageSize
	 */
	public void createVsttmMvAvgAmt(int pageSize) {
		log.info("5일 이동평균금액 {}건 생성 시작", pageSize);
		
		List<ItmTrd> itmTrdLst = itmTrdRepo.findByVsttmMvAvgAmtIsNull(PageRequest.of(0, pageSize, Sort.by("itmCd").ascending().and(Sort.by("dt").ascending())));
		
		if(!itmTrdLst.isEmpty()) {
			itmTrdLst.stream().forEach(itm -> {
				itm.setVsttmMvAvgAmt(createMvAvgAmt(itm, 5));
			});
			
			itmTrdRepo.saveAllAndFlush(itmTrdLst);
		}
		
		log.info("5일 이동평균금액 {}건 생성 종료", pageSize);
	}
	
	/**
	 * 단기 이동평균금액(20일 이동평균금액) 생성
	 * @param pageSize
	 */
	public void createSttmMvAvgAmt(int pageSize) {
		log.info("20일 이동평균금액 {}건 생성 시작", pageSize);
		
		List<ItmTrd> itmTrdLst = itmTrdRepo.findBySttmMvAvgAmtIsNull(PageRequest.of(0, pageSize, Sort.by("itmCd").ascending().and(Sort.by("dt").ascending())));
		
		if(!itmTrdLst.isEmpty()) {
			itmTrdLst.stream().forEach(itm -> {
				itm.setSttmMvAvgAmt(createMvAvgAmt(itm, 20));
			});
			
			itmTrdRepo.saveAllAndFlush(itmTrdLst);
		}
		
		log.info("20일 이동평균금액 {}건 생성 종료", pageSize);
	}
	
	/**
	 * 중기 이동평균금액(60일 이동평균금액) 생성
	 * @param pageSize
	 */
	public void createMdtmMvAvgAmt(int pageSize) {
		log.info("60일 이동평균금액 {}건 생성 시작", pageSize);
		
		List<ItmTrd> itmTrdLst = itmTrdRepo.findByMdtmMvAvgAmtIsNull(PageRequest.of(0, pageSize, Sort.by("itmCd").ascending().and(Sort.by("dt").ascending())));
		
		if(!itmTrdLst.isEmpty()) {
			itmTrdLst.stream().forEach(itm -> {
				itm.setMdtmMvAvgAmt(createMvAvgAmt(itm, 60));
			});
			
			itmTrdRepo.saveAllAndFlush(itmTrdLst);
		}
		
		log.info("60일 이동평균금액 {}건 생성 종료", pageSize);
	}
	
	/**
	 * 장기 이동평균금액(120일 이동평균금액) 생성
	 * @param pageSize
	 */
	public void createLntmMvAvgAmt(int pageSize) {
		log.info("120일 이동평균금액 {}건 생성 시작", pageSize);
		
		List<ItmTrd> itmTrdLst = itmTrdRepo.findByLntmMvAvgAmtIsNull(PageRequest.of(0, pageSize, Sort.by("itmCd").ascending().and(Sort.by("dt").ascending())));
		
		if(!itmTrdLst.isEmpty()) {
			itmTrdLst.stream().forEach(itm -> {
				itm.setLntmMvAvgAmt(createMvAvgAmt(itm, 120));
			});
			
			itmTrdRepo.saveAllAndFlush(itmTrdLst);
		}
		
		log.info("120일 이동평균금액 {}건 생성 종료", pageSize);
	}
	
	/**
	 * scale 만큼 평균을 구한다.
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
