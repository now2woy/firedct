package ji.hs.firedct.batch.itmfincsts.svc;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.dart.dartfnltt.entity.DartFnltt;
import ji.hs.firedct.data.dart.dartfnltt.repository.DartFnlttRepository;
import ji.hs.firedct.data.stock.cd.entity.Cd;
import ji.hs.firedct.data.stock.cd.repository.CdRepository;
import ji.hs.firedct.data.stock.itm.entity.Itm;
import ji.hs.firedct.data.stock.itm.repository.ItmRepository;
import ji.hs.firedct.data.stock.itmfincsts.entity.ItmFincSts;
import ji.hs.firedct.data.stock.itmfincsts.repository.ItmFincStsRepository;
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
	private CdRepository cdRepo;
	
	@Autowired
	private ItmRepository itmRepo;
	
	@Autowired
	private ItmFincStsRepository itmFincStsRepo;
	
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
	 * 데이터 정비용 ACNT_ID
	 */
	private Map<String, String> ACNT_ID_CD;
	
	/**
	 * 데이터 정비용 ACNT_NM
	 */
	private Map<String, String> ACNT_NM_CD;
	
	/**
	 * Dart 년도 / 분기 재무제표 수집
	 * 
	 * @param yr
	 * @param qt
	 * @param itmCd
	 * @param isDb
	 */
	public void crawling(final String yr, final String qt, final String itmCd, boolean isDb) throws Exception {
		log.info("{}년도 {}분기 재무제표 수집 시작", yr, qt);
		ACNT_ID_CD = new HashMap<>();
		ACNT_NM_CD = new HashMap<>();
		
		// ACNT_ID_CD 조회
		List<Cd> cds = cdRepo.findByCls("00007");
		
		// ACNT_ID_CD Map에 담는다.
		cds.stream().forEach(cd -> {
			ACNT_ID_CD.put(cd.getCdNm(), cd.getCdSubNm());
		});
		
		// ACNT_NM_CD 조회
		cds = cdRepo.findByCls("00008");
		
		// ACNT_NM_CD Map에 담는다.
		cds.stream().forEach(cd -> {
			ACNT_NM_CD.put(cd.getCdNm(), cd.getCdSubNm());
		});
		
		// Dart 종목 코드를 가진 모든 자료 조회
		List<Itm> itms = null;
		
		// 종목 재무제표 List
		final List<ItmFincSts> itmFincStss = new ArrayList<>();
		
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
		
		// 4분기 일때 년도를 변경 해야 되서 임시 변수 생성
		String tempYr = yr;
		
		// 4분기 일때 년도를 + 1 한다.
		if("4".equals(qt)){
			tempYr = String.valueOf(Integer.parseInt(tempYr) + 1);
		}
		
		final Date stdDt = Utils.dateParse(tempYr + cdRepo.findCdNmByClsAndCd("00006", cdRepo.findCdByClsAndCdSubNm("00004", qt)));
		
		if(isDb) {
			itmFincStsDb(yr, qt, stdDt, itms, itmFincStss);
		}else {
			itmFincStsDart(yr, qt, stdDt, itms, itmFincStss);
		}
		
		itmFincStss.stream().forEach(itmFincSts -> {
			// 자본 또는 지배자본과 부채가 있을 경우
			if((itmFincSts.getBscCpt() != null || itmFincSts.getOwnCpt() != null) && itmFincSts.getDebtAmt() != null) {
				// 부채가 0이 아닐 경우
				if(itmFincSts.getDebtAmt().compareTo(BigDecimal.ZERO) != 0) {
					// 자본이 0이 아닐 경우
					if(itmFincSts.getBscCpt() != null && itmFincSts.getBscCpt().compareTo(BigDecimal.ZERO) != 0) {
						/** 부채비율 = 부채총계 / 자본 * 100 */
						itmFincSts.setDebtRt(Utils.multiply(Utils.divide(itmFincSts.getDebtAmt(), itmFincSts.getBscCpt(), 10), new BigDecimal("100"), 2));
						
					// 지배자본이 0이 아닐 경우
					} else if(itmFincSts.getOwnCpt() != null && itmFincSts.getOwnCpt().compareTo(BigDecimal.ZERO) != 0) {
						/** 부채비율 = 부채총계 / 지배자본 * 100 */
						itmFincSts.setDebtRt(Utils.multiply(Utils.divide(itmFincSts.getDebtAmt(), itmFincSts.getOwnCpt(), 10), new BigDecimal("100"), 2));
					}
				}
			}
			
			// TODO 요기 일단 처리 안됨
			// 당기순이익이 없고 법인세비용차감전순이익과 법인세비용이 있을 경우 계산한다.
			if(itmFincSts.getTsNetIncmAmt() == null && StringUtils.isNotEmpty(itmFincSts.getTemp1()) && StringUtils.isNotEmpty(itmFincSts.getTemp2())){
				/** 당기순이익 = 법인세비용차감전순이익 - 법인세비용 */
				itmFincSts.setTsNetIncmAmt(new BigDecimal(itmFincSts.getTemp1()).subtract(new BigDecimal(itmFincSts.getTemp2())));
			}
			
			// 4분기 데이터의 경우 동년 전분기 데이터로 빼야 한다.
			if("4".equals(itmFincSts.getQt())) {
				qtDataSubtract(itmFincSts);
			}
			
			List<ItmFincSts> tmpItmFincStss = itmFincStsRepo.findByItmCdAndStdDtLessThan(itmFincSts.getItmCd(), stdDt, PageRequest.of(0, 3, Sort.by("stdDt").descending()));
			
			if(tmpItmFincStss.size() == 3) {
				// 데이터가 있을 경우 이번분기값을 넣는다.
				if(itmFincSts.getTsNetIncmAmt() != null) {
					itmFincSts.setSumTsNetIncmAmt(itmFincSts.getTsNetIncmAmt());
					itmFincSts.setSumTsNetIncmAmtCnt(1);
				}
				
				// 데이터가 있을 경우 이번분기값을 넣는다.
				if(itmFincSts.getOwnTsNetIncmAmt() != null) {
					itmFincSts.setSumOwnTsNetIncmAmt(itmFincSts.getOwnTsNetIncmAmt());
					itmFincSts.setSumOwnTsNetIncmAmtCnt(1);
				}
				
				tmpItmFincStss.stream().forEach(tmp -> {
					// 당기순이익이 있을 경우
					itmFincSts.setSumTsNetIncmAmt(Utils.add(itmFincSts.getSumTsNetIncmAmt(), tmp.getTsNetIncmAmt()));
					itmFincSts.setSumTsNetIncmAmtCnt(Utils.addCnt(itmFincSts.getSumTsNetIncmAmtCnt(), itmFincSts.getSumTsNetIncmAmt(), tmp.getTsNetIncmAmt()));
					
					// 지배당기순이익이 있을 경우
					itmFincSts.setSumOwnTsNetIncmAmt(Utils.add(itmFincSts.getSumOwnTsNetIncmAmt(), tmp.getOwnTsNetIncmAmt()));
					itmFincSts.setSumOwnTsNetIncmAmtCnt(Utils.addCnt(itmFincSts.getSumOwnTsNetIncmAmtCnt(), itmFincSts.getSumOwnTsNetIncmAmt(), tmp.getOwnTsNetIncmAmt()));
				});
				
				// 지배자본이 있을 경우
				if(itmFincSts.getOwnCpt() != null) {
					// 합계 지배당기순이익이 있고 합계 횟수가 4번일 경우
					if(itmFincSts.getSumOwnTsNetIncmAmt() != null && itmFincSts.getSumOwnTsNetIncmAmtCnt() == 4) {
						/** ROE = (지배당기순이익 / 지배자본) * 100 */
						itmFincSts.setRoe(Utils.multiply(Utils.divide(itmFincSts.getSumOwnTsNetIncmAmt(), itmFincSts.getOwnCpt(), 10), new BigDecimal("100"), 2));
						
					// 합계 당기순이익이 있고 합계 횟수가 4번일 경우
					}else if(itmFincSts.getSumTsNetIncmAmt() != null && itmFincSts.getSumTsNetIncmAmtCnt() == 4) {
						/** ROE = (당기순이익 / 지배자본) * 100 */
						itmFincSts.setRoe(Utils.multiply(Utils.divide(itmFincSts.getSumTsNetIncmAmt(), itmFincSts.getOwnCpt(), 10), new BigDecimal("100"), 2));
					}
					
				// 자본이 있을 경우
				}else if(itmFincSts.getBscCpt() != null) {
					// 합계 지배당기순이익이 있고 합계 횟수가 4번일 경우
					if(itmFincSts.getSumOwnTsNetIncmAmt() != null && itmFincSts.getSumOwnTsNetIncmAmtCnt() == 4) {
						/** ROE = (지배당기순이익 / 자본) * 100 */
						itmFincSts.setRoe(Utils.multiply(Utils.divide(itmFincSts.getSumOwnTsNetIncmAmt(), itmFincSts.getBscCpt(), 10), new BigDecimal("100"), 2));
						
					// 합계 당기순이익이 있고 합계 횟수가 4번일 경우
					}else if(itmFincSts.getSumTsNetIncmAmt() != null && itmFincSts.getSumTsNetIncmAmtCnt() == 4) {
						/** ROE = (당기순이익 / 자본) * 100 */
						itmFincSts.setRoe(Utils.multiply(Utils.divide(itmFincSts.getSumTsNetIncmAmt(), itmFincSts.getBscCpt(), 10), new BigDecimal("100"), 2));
					}
				}
				
				// 자산총계가 있을 경우
				if(itmFincSts.getAstAmt() != null) {
					// 합계 지배당기순이익이 있고 합계 횟수가 4번일 경우
					if(itmFincSts.getSumOwnTsNetIncmAmt() != null && itmFincSts.getSumOwnTsNetIncmAmtCnt() == 4) {
						/** ROA = (지배당기순이익 / 자산총계) * 100 */
						itmFincSts.setRoa(Utils.multiply(Utils.divide(itmFincSts.getSumOwnTsNetIncmAmt(), itmFincSts.getAstAmt(), 10), new BigDecimal("100"), 2));
						
					// 합계 당기순이익이 있고 합계 횟수가 4번일 경우
					}else if(itmFincSts.getSumTsNetIncmAmt() != null && itmFincSts.getSumTsNetIncmAmtCnt() == 4) {
						/** ROA = (당기순이익 / 자산총계) * 100 */
						itmFincSts.setRoa(Utils.multiply(Utils.divide(itmFincSts.getSumTsNetIncmAmt(), itmFincSts.getAstAmt(), 10), new BigDecimal("100"), 2));
					}
				}
			}
			
			// 년도가 같고 현재 분기 이전 데이터 조회
			tmpItmFincStss = itmFincStsRepo.findByItmCdAndYrAndQtLessThan(itmFincSts.getItmCd(), itmFincSts.getYr(), itmFincSts.getQt());
			
			// 데이터가 있을 경우 반복
			tmpItmFincStss.stream().forEach(tmpItmFincSts -> {
				/** 영업활동현금흐름 =  당기 영업활동현금흐름 - 이전분기 영업활동현금흐름 */
				itmFincSts.setOprCsflw(Utils.subtract(itmFincSts.getOprCsflw(), tmpItmFincSts.getOprCsflw()));
			});
		});
		
		itmFincStsRepo.saveAllAndFlush(itmFincStss);
		
		log.info("{}년도 {}분기 재무제표 수집 종료", yr, qt);
	}
	
	/**
	 * DB에서 조회하여 분기 제무 데이터 처리
	 * @param yr
	 * @param qt
	 * @param stdDt
	 * @param itms
	 * @param itmFincStss
	 */
	private void itmFincStsDb(final String yr, final String qt, final Date stdDt, final List<Itm> itms, final List<ItmFincSts> itmFincStss) {
		itms.stream().forEach(itm -> {
			List<DartFnltt> dartFnltts = dartFnlttRepo.findByItmCdAndYrAndQtOrderBySeqAsc(itm.getItmCd(), yr, qt);
			
			// DART에 데이터가 업는 경우가 있음
			if(!dartFnltts.isEmpty()) {
				ItmFincSts itmFincSts = new ItmFincSts();
				
				itmFincSts.setItmCd(itm.getItmCd());
				itmFincSts.setMkt(itm.getMkt());
				itmFincSts.setYr(yr);
				itmFincSts.setQt(qt);
				itmFincSts.setStdDt(stdDt);
				
				dartFnltts.stream().forEach(dartFnltt -> {
					setItmFincSts(itmFincSts, dartFnltt);
				});
				
				itmFincStss.add(itmFincSts);
				
				log.info("{}", itmFincSts.toString());
			}
		});
	}
	
	/**
	 * Dart에서 조회하여 분기 제무 데이터 처리
	 * @param yr
	 * @param qt
	 * @param stdDt
	 * @param itms
	 * @param itmFincStss
	 */
	private void itmFincStsDart(final String yr, final String qt, final Date stdDt, final List<Itm> itms, final List<ItmFincSts> itmFincStss) {
		final ObjectMapper mapper = new ObjectMapper();
		
		// 대상 목록 루핑
		itms.stream().forEach(itm -> {
			try{
				Map<String, Object> map = mapper.readValue(getDataFromUrl(itm.getDartItmCd(), yr, qt, "CFS"), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
				
				// 
				if("013".equals(map.get("status"))) {
					map = mapper.readValue(getDataFromUrl(itm.getDartItmCd(), yr, qt, "OFS"), mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
					
				// 사용한도를 초과
				}else if("020".equals(map.get("status"))) {
					log.info("{}, {}", "사용한도를 초과하였습니다.", itm.toString());
				}
				
				// URL 조회 결과가 정상일 경우
				if("000".equals(map.get("status"))) {
					List<Map<String, String>> blockLst = (ArrayList<Map<String, String>>)map.get("list");
					
					ItmFincSts itmFincSts = new ItmFincSts();
					
					itmFincSts.setItmCd(itm.getItmCd());
					itmFincSts.setMkt(itm.getMkt());
					itmFincSts.setYr(yr);
					itmFincSts.setQt(qt);
					itmFincSts.setStdDt(stdDt);
					
					blockLst.stream().forEach(block -> {
						setItmFincSts(itmFincSts, block.get("sj_div"), block.get("account_id"), block.get("account_nm"), block.get("account_detail"), block.get("thstrm_amount"), block.get("thstrm_add_amount"));
					});
					
					itmFincStss.add(itmFincSts);
					
					log.info("{}", itmFincSts.toString());
				}
			}catch(Exception e) {
				log.error("", e);
			}
		});
	}
	
	/**
	 * 4분기 데이터 정리
	 * @param itmFincSts
	 */
	private void qtDataSubtract(ItmFincSts itmFincSts) {
		List<ItmFincSts> yrItmFincStss = itmFincStsRepo.findByItmCdAndYrAndQtLessThan(itmFincSts.getItmCd(), itmFincSts.getYr(), itmFincSts.getQt());
		Optional<ItmFincSts> trdItmFincSts = itmFincStsRepo.findByItmCdAndYrAndQt(itmFincSts.getItmCd(), itmFincSts.getYr(), "3");
		
		if(trdItmFincSts.isPresent()) {
			
			Map<String, BigDecimal> addListNumber = new HashMap<>();
			
			yrItmFincStss.stream().forEach(yrItmFincSts -> {
				// 1분기부터 3분기까지 매출액 합계
				addListNumber.put("salAmt", Utils.add(addListNumber.get("salAmt"), yrItmFincSts.getSalAmt()));
				
				// 1분기부터 3분기까지 영업이익금액 합계
				addListNumber.put("oprIncmAmt", Utils.add(addListNumber.get("oprIncmAmt"), yrItmFincSts.getOprIncmAmt()));
				
				// 1분기부터 3분기까지 당기순이익금액 합계
				addListNumber.put("tsNetIncmAmt", Utils.add(addListNumber.get("tsNetIncmAmt"), yrItmFincSts.getTsNetIncmAmt()));
				
				// 1분기부터 3분기까지 지배주주당기순이익금액 합계
				addListNumber.put("ownTsNetIncmAmt", Utils.add(addListNumber.get("ownTsNetIncmAmt"), yrItmFincSts.getOwnTsNetIncmAmt()));
				
				// 1분기부터 3분기까지 당기기본주당순이익 합계
				addListNumber.put("tsBscEps", Utils.add(addListNumber.get("tsBscEps"), yrItmFincSts.getTsBscEps()));
				
				// 1분기부터 3분기까지 당기희석주당순이익 합계
				addListNumber.put("tsDltdEps", Utils.add(addListNumber.get("tsDltdEps"), yrItmFincSts.getTsDltdEps()));
				
				// 1분기부터 3분기까지 계속영업기본주당순이익 합계
				addListNumber.put("oprBscEps", Utils.add(addListNumber.get("oprBscEps"), yrItmFincSts.getOprBscEps()));
				
				// 1분기부터 3분기까지 계속영업희석주당순이익 합계
				addListNumber.put("oprDltdEps", Utils.add(addListNumber.get("oprDltdEps"), yrItmFincSts.getOprDltdEps()));
			});
			
			// 매출액 계산
			itmFincSts.setSalAmt(Utils.subtract(itmFincSts.getSalAmt(), trdItmFincSts.get().getAddSalAmt(), addListNumber.get("salAmt")));
			
			// 영업이익금액 계산
			itmFincSts.setOprIncmAmt(Utils.subtract(itmFincSts.getOprIncmAmt(), trdItmFincSts.get().getAddOprIncmAmt(), addListNumber.get("oprIncmAmt")));
			
			// 당기순이익금액 계산
			itmFincSts.setTsNetIncmAmt(Utils.subtract(itmFincSts.getTsNetIncmAmt(), trdItmFincSts.get().getAddTsNetIncmAmt(), addListNumber.get("tsNetIncmAmt")));
			
			// 지배주주당기순이익금액 계산
			itmFincSts.setOwnTsNetIncmAmt(Utils.subtract(itmFincSts.getOwnTsNetIncmAmt(), trdItmFincSts.get().getAddOwnTsNetIncmAmt(), addListNumber.get("ownTsNetIncmAmt")));
			
			// 당기기본주당순이익 계산
			itmFincSts.setTsBscEps(Utils.subtract(itmFincSts.getTsBscEps(), trdItmFincSts.get().getAddTsBscEps(), addListNumber.get("tsBscEps")));
			
			// 당기희석주당순이익 계산
			itmFincSts.setTsDltdEps(Utils.subtract(itmFincSts.getTsDltdEps(), trdItmFincSts.get().getAddTsDltdEps(), addListNumber.get("tsDltdEps")));
			
			// 계속영업기본주당순이익 계산
			itmFincSts.setOprBscEps(Utils.subtract(itmFincSts.getOprBscEps(), trdItmFincSts.get().getAddOprBscEps(), addListNumber.get("oprBscEps")));
			
			// 계속영업희석주당순이익 계산
			itmFincSts.setOprDltdEps(Utils.subtract(itmFincSts.getOprDltdEps(), trdItmFincSts.get().getAddOprDltdEps(), addListNumber.get("oprDltdEps")));
		}
	}
	
	/**
	 * 
	 * @param acntId
	 * @return
	 */
	private String getAcntIdCd(final String acntId) {
		if(ACNT_ID_CD.containsKey(acntId)) {
			return ACNT_ID_CD.get(acntId);
		}else {
			return "0";
		}
	}
	
	/**
	 * 
	 * @param acntNm
	 * @return
	 */
	private String getAcntNmCd(final String acntNm) {
		if(ACNT_NM_CD.containsKey(acntNm)) {
			return ACNT_NM_CD.get(acntNm);
		}else {
			return "0";
		}
	}
	
	/**
	 * 코드값에 맞춰 데이터를 매핑한다.
	 * @param itmFincSts
	 * @param dartFnltt
	 */
	private void setItmFincSts(ItmFincSts itmFincSts, DartFnltt dartFnltt) {
		setItmFincSts(itmFincSts, dartFnltt.getSjDiv(), dartFnltt.getAcntId(), dartFnltt.getAcntNm(), dartFnltt.getAcntDtl(), dartFnltt.getThTmAmt(), dartFnltt.getThTmAddAmt());
	}
	
	/**
	 * 코드값에 맞춰 데이터를 매핑한다.
	 * @param itmFincSts
	 * @param sjDiv
	 * @param acntId
	 * @param acntNm
	 * @param acntDtl
	 * @param thTmAmt
	 * @param thTmAddAmt
	 */
	private void setItmFincSts(ItmFincSts itmFincSts, final String sjDiv, final String acntId, final String acntNm, final String acntDtl, final String thTmAmt, final String thTmAddAmt) {
		String cd = getAcntIdCd(acntId);
		
		if("9999".equals(cd)) {
			cd = getAcntNmCd(acntNm);
		}
		
		// 손익계산서와 포괄손익계산서 일 경우
		if("IS".equals(sjDiv) || "CIS".equals(sjDiv)) {
			switch (cd) {
				// 매출액
				case "100":
					itmFincSts.setSalAmt(Utils.stringToBigDecimal(thTmAmt));
					itmFincSts.setAddSalAmt(Utils.stringToBigDecimal(thTmAddAmt));
					break;
					
				// 매출총이익 태그에 영업수익을 입력한 경우가 있어서 부분적으로 사용
				case "110":
					if("영업수익".equals(acntNm)
					|| "I.영업수익".equals(acntNm)
					|| "영업수익(매출액)".equals(acntNm)
					|| "매출총이익(영업수익)".equals(acntNm)){
						itmFincSts.setSalAmt(Utils.stringToBigDecimal(thTmAmt));
						itmFincSts.setAddSalAmt(Utils.stringToBigDecimal(thTmAddAmt));
					}
					break;
					
				// 영업이익
				case "200":
					itmFincSts.setOprIncmAmt(Utils.stringToBigDecimal(thTmAmt));
					itmFincSts.setAddOprIncmAmt(Utils.stringToBigDecimal(thTmAddAmt));
					break;
					
				// 당기순이익
				case "300":
					// 당기순이익의 경우 여러 태그가 있는데 이중 두가지
					if("연결재무제표 [member]".equals(acntDtl)
					|| "-".equals(acntDtl)){
						itmFincSts.setTsNetIncmAmt(Utils.stringToBigDecimal(thTmAmt));
						itmFincSts.setAddTsNetIncmAmt(Utils.stringToBigDecimal(thTmAddAmt));
					}
					break;
					
				// 지배 당기순이익
				case "400":
					if("-".equals(acntDtl)) {
						// 동일한 태그가 반복 되어 이전 태그값을 가져오기 위해 제한
						// 226360 이놈 때문임
						if(itmFincSts.getOwnTsNetIncmAmt() == null) {
							itmFincSts.setOwnTsNetIncmAmt(Utils.stringToBigDecimal(thTmAmt));
						}
						itmFincSts.setAddOwnTsNetIncmAmt(Utils.stringToBigDecimal(thTmAddAmt));
					}
					break;
					
				// 당기기본주당순이익
				case "700":
					if("-".equals(acntDtl)) {
						itmFincSts.setTsBscEps(Utils.stringToBigDecimal(thTmAmt));
						itmFincSts.setAddTsBscEps(Utils.stringToBigDecimal(thTmAddAmt));
					}
					break;
					
				// 당기희석주당순이익
				case "800":
					if("-".equals(acntDtl)) {
						itmFincSts.setTsDltdEps(Utils.stringToBigDecimal(thTmAmt));
						itmFincSts.setAddTsDltdEps(Utils.stringToBigDecimal(thTmAddAmt));
					}
					break;
					
				// 당기기본주당순이익 + 당기희석주당순이익
				case "810":
					if("-".equals(acntDtl)) {
						// 당기기본주당순이익
						itmFincSts.setTsBscEps(Utils.stringToBigDecimal(thTmAmt));
						// 당기희석주당순이익
						itmFincSts.setTsDltdEps(Utils.stringToBigDecimal(thTmAmt));
						// 당기기본주당순이익 합계
						itmFincSts.setAddTsBscEps(Utils.stringToBigDecimal(thTmAddAmt));
						// 당기희석주당순이익 합계
						itmFincSts.setAddTsDltdEps(Utils.stringToBigDecimal(thTmAddAmt));
					}
					break;
					
				// 당기기본주당순이익 + 당기희석주당순이익, 특정태그만 해당이라 한번 더 필터링
				case "820":
					if("기본 및 희석 주당이익".equals(acntNm)) {
						if("-".equals(acntDtl)) {
							itmFincSts.setTsBscEps(Utils.stringToBigDecimal(thTmAmt));
							itmFincSts.setTsDltdEps(Utils.stringToBigDecimal(thTmAmt));
							itmFincSts.setAddTsBscEps(Utils.stringToBigDecimal(thTmAddAmt));
							itmFincSts.setAddTsDltdEps(Utils.stringToBigDecimal(thTmAddAmt));
						}
					}
					break;
					
				// 계속영업기본주당순이익
				case "900":
					itmFincSts.setOprBscEps(Utils.stringToBigDecimal(thTmAmt));
					itmFincSts.setAddOprBscEps(Utils.stringToBigDecimal(thTmAddAmt));
					break;
					
				// 계속영업희석주당순이익
				case "1000":
					itmFincSts.setOprDltdEps(Utils.stringToBigDecimal(thTmAmt));
					itmFincSts.setAddOprDltdEps(Utils.stringToBigDecimal(thTmAddAmt));
					break;
					
				// 계속영업기본주당순이익 + 계속영업희석주당순이익
				case "1010":
					itmFincSts.setOprBscEps(Utils.stringToBigDecimal(thTmAmt));
					itmFincSts.setOprDltdEps(Utils.stringToBigDecimal(thTmAmt));
					itmFincSts.setAddOprBscEps(Utils.stringToBigDecimal(thTmAddAmt));
					itmFincSts.setAddOprDltdEps(Utils.stringToBigDecimal(thTmAddAmt));
					break;
					
				default:
					break;
			}
		}else if("BS".equals(sjDiv)) {
			switch (cd) {
				// 자본
				case "1100":
					itmFincSts.setBscCpt(Utils.stringToBigDecimal(thTmAmt));
					break;
					
				// 지배자본
				case "1200":
					itmFincSts.setOwnCpt(Utils.stringToBigDecimal(thTmAmt));
					break;
					
				// 자산총계
				case "1300":
					itmFincSts.setAstAmt(Utils.stringToBigDecimal(thTmAmt));
					break;
					
				// 부채총계
				case "1400":
					itmFincSts.setDebtAmt(Utils.stringToBigDecimal(thTmAmt));
					break;
					
				default:
					break;
			}
		} else if("CF".equals(sjDiv)) {
			switch (cd) {
				// 영업활동현금흐름
				case "1500":
					itmFincSts.setOprCsflw(Utils.stringToBigDecimal(thTmAmt));
					break;
					
				default:
					break;
			}
		}
	}
	
	/**
	 * DART URL로 부터 데이터를 가져온다.
	 * 
	 * @param itmCd
	 * @param yr
	 * @param qtCd
	 * @return
	 */
	private String getDataFromUrl(final String itmCd, final String yr, final String qt, final String fsDiv) {
		String result = null;
		String qtCd = cdRepo.findCdByClsAndCdSubNm("00004", qt);
		InputStream is = null;
		
		try {
			// 1분에 1000건이 넘으면 24시간 차단을 당하기 때문에 100ms 슬립한다.
			Thread.sleep(100L);
			
			URL url = new URL(dartFincStsUrl + "?crtfc_key=" + dartApiKey + "&corp_code=" + itmCd + "&bsns_year=" + yr + "&reprt_code=" + qtCd + "&fs_div=CFS" + fsDiv);
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