package ji.hs.firedct.batch.itmtrd.svc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.batch.tactic.svc.Tactic000Service;
import ji.hs.firedct.batch.tactic.svc.Tactic020Service;
import ji.hs.firedct.batch.tactic.svc.Tactic024Service;
import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.cd.entity.Cd;
import ji.hs.firedct.data.stock.cd.repository.CdRepository;
import ji.hs.firedct.data.stock.itm.repository.ItmRepository;
import ji.hs.firedct.data.stock.itmfincsts.entity.ItmFincSts;
import ji.hs.firedct.data.stock.itmfincsts.repository.ItmFincStsRepository;
import ji.hs.firedct.data.stock.itmtrd.entity.ItmTrd;
import ji.hs.firedct.data.stock.itmtrd.repository.ItmTrdRepository;
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
	
	@Autowired
	private ItmFincStsRepository itmFincStsRepo;
	
	@Autowired
	private Tactic000Service tactic000Service;
	
	@Autowired
	private Tactic020Service tactic020Service;
	
	@Autowired
	private Tactic024Service tactic024Service;
	
	/**
	 * 거래소에서 JSON 데이터를 가져오는 URL
	 */
	@Value("${constant.url.krxJson}")
	private String krxJsonUrl;
	
	/**
	 * S
	 * 
	 * 최종수집일 + 1부터 현재일까지 KRX 일자별 종목 시세 수집
	 * 매일 수집하는 스케쥴에서 사용
	 */
	public void crawling() {
		try {
			final List<String> dtLst = findMaxDtByItmCd();
			
			dtLst.stream().forEach(dt -> {
				itmTrdCrawlingByDt(dt);
				createPer(dt);
				createBPSAndPBRAndSPSAndPSR(dt);
				tactic000Service.publishing(dt);
				tactic020Service.publishing(dt);
				tactic024Service.publishing(dt);
				
			});
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 최종 수집일 다음날 KRX 일자별 종목 시세 수집
	 * 2007년 1월 2일 부터 현재일까지 수집할때 사용
	 */
	public void itmTrdCrawlingByLastDayAfter() {
		Page<ItmTrd> itmTrd = itmTrdRepo.findAll(PageRequest.of(0, 1, Sort.by("dt").descending()));
		
		// 조회된 데이터가 없을 경우 2007년 1월 2일 부터 수집
		if(itmTrd.isEmpty()) {
			itmTrdCrawlingByDt("20070102");
		}else {
			// 현재일
			final int currDate = Integer.parseInt(Utils.dateFormat(new Date()));
			
			// 가져온 날자 + 1
			Date findDt = DateUtils.addDays(itmTrd.toList().get(0).getDt(), 1);
			
			while(true) {
				// 현재일보다 작거나 같을 경우
				if(currDate >= Integer.parseInt(Utils.dateFormat(findDt))) {
					// 거래내역이 있는지 확인
					if(!isTrdDt(Utils.dateFormat(findDt))) {
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
			if(currDate >= Integer.parseInt(Utils.dateFormat(findDt))) {
				itmTrdCrawlingByDt(Utils.dateFormat(findDt));
			}
		}
	}
	
	/**
	 * 일자에 거래내역이 있는지 확인
	 * @param dt
	 * @return
	 */
	private boolean isTrdDt(final String dt) {
		final String mkt = "STK";
		boolean isTrdDt = false;
		
		try {
			Document doc = Jsoup.connect(krxJsonUrl)
							.data("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
							.data("mktId", mkt)
							.data("trdDd", dt)
							.get();
			
			Map<String, Object> map = Utils.jsonParse(doc.text());
			
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
	 * KRX 일자별 종목 시세 수집
	 * @param dt
	 */
	private void itmTrdCrawlingByDt(final String dt) {
		final List<Cd> mktLst = cdRepo.findByCls("00001");
		
		mktLst.stream().forEach(mkt -> {
			log.info("{}일 {} 수집 시작", dt, mkt.getCd());
			
			try {
				final List<ItmTrd> itmLst = new ArrayList<>();
				
				Document doc = Jsoup.connect(krxJsonUrl)
								.data("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
								.data("mktId", mkt.getCd())
								.data("trdDd", dt)
								.get();
				
				Map<String, Object> map = Utils.jsonParse(doc.text());
				
				List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("OutBlock_1");
				
				blockLst.stream().forEach(itm -> {
					
					// 휴일엔 종가가 없다.
					if(!"-".equals(itm.get("TDD_CLSPRC"))) {
						ItmTrd itmTrd = new ItmTrd();
						
						try {
							if(itmRepo.findByItmCd(itm.get("ISU_SRT_CD")).isPresent()) {
								itmTrd.setItmCd(itm.get("ISU_SRT_CD"));
								itmTrd.setDt(Utils.dateParse(dt));
								itmTrd.setEdAmt(new BigDecimal(itm.get("TDD_CLSPRC").replaceAll(",", "")));
								itmTrd.setIncr(new BigDecimal(itm.get("CMPPREVDD_PRC").replaceAll(",", "")));
								itmTrd.setTrdQty(new BigDecimal(itm.get("ACC_TRDVOL").replaceAll(",", "")));
								itmTrd.setTrdAmt(new BigDecimal(itm.get("ACC_TRDVAL").replaceAll(",", "")));
								itmTrd.setMktTotAmt(new BigDecimal(itm.get("MKTCAP").replaceAll(",", "")));
								itmTrd.setIsuStkQty(new BigDecimal(itm.get("LIST_SHRS").replaceAll(",", "")));
								
								if(itmTrd.getTrdQty().compareTo(BigDecimal.ZERO) > 0 && itmTrd.getIsuStkQty().compareTo(BigDecimal.ZERO) > 0) {
									// ROUND((당일거래수 / 전체주식수) * 100, 2)
									itmTrd.setTrdTnovRt(Utils.multiply(Utils.divide(itmTrd.getTrdQty(), itmTrd.getIsuStkQty(), 10), new BigDecimal("100"), 2));
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
	 * 최종거래일자부터 현재일자까지 List
	 * 
	 * @param format
	 * @param itmCd
	 * @return
	 */
	private List<String> findMaxDtByItmCd() throws Exception {
		final List<String> dateLst = new ArrayList<>();
		final int currDate = Integer.parseInt(Utils.dateFormat(new Date()));
		
		// 최종거래일 조회
		Page<ItmTrd> itmTrd = itmTrdRepo.findAll(PageRequest.of(0, 1, Sort.by("dt").descending()));
		
		Date lastCrawlingdate = null;
		
		// 조회된 데이터가 없을 경우 2007년 1월 2일 부터 수집
		if(itmTrd.isEmpty()) {
			lastCrawlingdate = Utils.dateParse("20070102");
			
		// 최종거래일이 있을 경우 사용
		}else {
			lastCrawlingdate = itmTrd.toList().get(0).getDt();
		}
		
		// 현재 일자보다 최종거래일이 작을 경우
		while(currDate > Integer.parseInt(Utils.dateFormat(lastCrawlingdate))) {
			// 최종거래일 + 1 하여 목록에 담는다.
			lastCrawlingdate = DateUtils.addDays(lastCrawlingdate, 1);
			dateLst.add(Utils.dateFormat(lastCrawlingdate));
			
			log.info("{}", Utils.dateFormat(lastCrawlingdate));
		}
		
		return dateLst;
	}
	
	/**
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
				mvAvgAmt = Utils.add(mvAvgAmt, itmTrdLst.get(i).getEdAmt());
			}
			
			mvAvgAmt = Utils.divide(mvAvgAmt, new BigDecimal(scale), 0);
		}
		
		return mvAvgAmt;
	}
	
	/**
	 * 해당일자의 PER을 생성한다.
	 * @param dt
	 */
	public void createPer(String dt) {
		try {
			log.info("{}일 PER 생성 시작", dt);
			
			List<ItmTrd> itmTrds = itmTrdRepo.findByDt(Utils.dateParse(dt));
			
			itmTrds.stream().forEach(itmTrd -> {
				// 4개 분기 데이터를 가져온다.
				List<ItmFincSts> itmFincStss = itmFincStsRepo.findByItmCdAndStdDtLessThanEqual(itmTrd.getItmCd(), Utils.dateParse(dt), PageRequest.of(0, 4, Sort.by("stdDt").descending()));
				
				// 4개 분기 일때만 처리 한다.
				if(itmFincStss.size() == 4) {
					itmTrd.setSumEpsCnt(0);
					
					itmFincStss.stream().forEach(itmFincSts -> {
						// 당기기본주당순이익이 있을 때
						if(itmFincSts.getTsBscEps() != null) {
							// 당기기본주당순이익을 EPS에 더한다.
							itmTrd.setSumEps(Utils.add(itmTrd.getSumEps(), itmFincSts.getTsBscEps()));
							itmTrd.setSumEpsCnt(Utils.addCnt(itmTrd.getSumEpsCnt(), itmTrd.getSumEps(), itmFincSts.getTsBscEps()));
							
						// 당기희석주당순이익이 있을 때
						}else if(itmFincSts.getTsDltdEps() != null) {
							// 당기희석주당순이익을 EPS에 더한다.
							itmTrd.setSumEps(Utils.add(itmTrd.getSumEps(), itmFincSts.getTsDltdEps()));
							itmTrd.setSumEpsCnt(Utils.addCnt(itmTrd.getSumEpsCnt(), itmTrd.getSumEps(), itmFincSts.getTsDltdEps()));
							
						// 계속영업기본주당순이익이 있을 때
						}else if(itmFincSts.getOprBscEps() != null) {
							// 계속영업기본주당순이익을 EPS에 더한다.
							itmTrd.setSumEps(Utils.add(itmTrd.getSumEps(), itmFincSts.getOprBscEps()));
							itmTrd.setSumEpsCnt(Utils.addCnt(itmTrd.getSumEpsCnt(), itmTrd.getSumEps(), itmFincSts.getOprBscEps()));
							
						// 계속영업희석주당순이익이 있을 때
						}else if(itmFincSts.getOprDltdEps() != null) {
							// 계속영업희석주당순이익을 EPS에 더한다.
							itmTrd.setSumEps(Utils.add(itmTrd.getSumEps(), itmFincSts.getOprDltdEps()));
							itmTrd.setSumEpsCnt(Utils.addCnt(itmTrd.getSumEpsCnt(), itmTrd.getSumEps(), itmFincSts.getOprDltdEps()));
						}
					});
					
					// 종가가 있고, EPS를 더한 횟수가 4번이고, EPS를 합한 결과가 0이 아닐 경우
					if(itmTrd.getEdAmt() != null && itmTrd.getSumEpsCnt() == 4 && itmTrd.getSumEps().compareTo(BigDecimal.ZERO) != 0) {
						// PER 생성
						itmTrd.setPer(Utils.divide(itmTrd.getEdAmt(), itmTrd.getSumEps(), 2));
						
					}
				}
			});
			
			// PER을 저장한다.
			itmTrdRepo.saveAllAndFlush(itmTrds);
			
			log.info("{}일 PER 생성 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * BPS, PBR, SPS, PSR 을 만든다.
	 * @param dt
	 */
	public void createBPSAndPBRAndSPSAndPSR(String dt) {
		try {
			log.info("{}일 BPS, CPS, SPS, PBR, PCR, PSR 생성 시작", dt);
			
			List<ItmTrd> itmTrds = itmTrdRepo.findByDt(Utils.dateParse(dt));
			
			itmTrds.stream().forEach(itmTrd -> {
				// 거래일 기준으로 직전 분기 자료를 조회한다.
				List<ItmFincSts> itmFincStss = itmFincStsRepo.findByItmCdAndStdDtLessThanEqual(itmTrd.getItmCd(), Utils.dateParse(dt), PageRequest.of(0, 4, Sort.by("stdDt").descending()));
				
				// 기본주, 우선주 정보를 가져온다.
				List<ItmTrd> qtys = itmTrdRepo.findByDtAndItmCdLike(Utils.dateParse(dt), itmTrd.getItmCd().substring(0, 5) + "%");
				
				// 기본주, 우선주의 주식 수를 더한다.
				qtys.stream().forEach(qty -> {
					itmTrd.setTotIsuStkQty(Utils.add(itmTrd.getTotIsuStkQty(), qty.getIsuStkQty()));
				});
				
				if(!itmFincStss.isEmpty()) {
					// 지배 자본이 있을 경우
					if(itmFincStss.get(0).getOwnCpt() != null && itmFincStss.get(0).getOwnCpt().compareTo(BigDecimal.ZERO) != 0) {
						/** BPS = 지배자본 / 발행주식수 */
						itmTrd.setBps(Utils.divide(itmFincStss.get(0).getOwnCpt(), itmTrd.getTotIsuStkQty(), 2));
						
					// 지배자본이 없을 경우 기본 자본이 있는지 확인
					}else if(itmFincStss.get(0).getBscCpt() != null && itmFincStss.get(0).getBscCpt().compareTo(BigDecimal.ZERO) != 0) {
						/** BPS = 자본 / 발행주식수 */
						itmTrd.setBps(Utils.divide(itmFincStss.get(0).getBscCpt(), itmTrd.getTotIsuStkQty(), 2));
					}
					
					/** PBR = 종가 / BPS */
					itmTrd.setPbr(Utils.divide(itmTrd.getEdAmt(), itmTrd.getBps(), 2));
					
					// 4개 분기 자료가 조회되었을 경우
					if(itmFincStss.size() == 4) {
						itmTrd.setSumSalAmtCnt(0);
						itmTrd.setSumOprCsflwCnt(0);
						
						itmFincStss.stream().forEach(itmFincSts -> {
							// 매출액을 더한다.
							itmTrd.setSumSalAmt(Utils.add(itmTrd.getSumSalAmt(), itmFincSts.getSalAmt()));
							itmTrd.setSumSalAmtCnt(Utils.addCnt(itmTrd.getSumSalAmtCnt(), itmTrd.getSumSalAmt(), itmFincSts.getSalAmt()));
							
							// 영업활동현금흐름을 더한다.
							itmTrd.setSumOprCsflw(Utils.add(itmTrd.getSumOprCsflw(), itmFincSts.getOprCsflw()));
							itmTrd.setSumOprCsflwCnt(Utils.addCnt(itmTrd.getSumOprCsflwCnt(), itmTrd.getSumOprCsflw(), itmFincSts.getOprCsflw()));
						});
						
						// 합계 횟수가 4번일때
						if(itmTrd.getSumSalAmtCnt() == 4) {
							/** SPS = 4분기 합계매출액 / 발행주식수 */
							itmTrd.setSps(Utils.divide(itmTrd.getSumSalAmt(), itmTrd.getTotIsuStkQty(), 2));
						}
						
						/** PSR = 종가 / SPS */
						itmTrd.setPsr(Utils.divide(itmTrd.getEdAmt(), itmTrd.getSps(), 2));
						
						// 합계 횟수가 4번일때
						if(itmTrd.getSumOprCsflwCnt() == 4) {
							/** CPS =  4분기 합계영업활동현금흐름 / 발행주식수 */
							itmTrd.setCps(Utils.divide(itmTrd.getSumOprCsflw(), itmTrd.getTotIsuStkQty(), 2));
						}
						
						/** PCR = 종가 / CPS */
						itmTrd.setPcr(Utils.divide(itmTrd.getEdAmt(), itmTrd.getCps(), 2));
					}
				}
			});
			
			// 이동평균금액을 저장한다.
			itmTrdRepo.saveAllAndFlush(itmTrds);
			
			log.info("{}일 BPS, CPS, SPS, PBR, PCR, PSR 생성 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
}