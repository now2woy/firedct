package ji.hs.firedct.tactic.svc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.itm.entity.Itm;
import ji.hs.firedct.data.stock.itm.repository.ItmRepository;
import ji.hs.firedct.data.stock.itmtrd.entity.ItmTrd;
import ji.hs.firedct.data.stock.itmtrd.repository.ItmTrdRepository;
import ji.hs.firedct.tactic.dao.TacticVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 전략 24을 구글 스프레드 시트로 출력한다.
 * @author now2woy
 *
 */
@Slf4j
@Service
public class Tactic024Service {
	@Autowired
	private ItmRepository itmRepo;
	
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	@Autowired
	private Tactic000 tactic000;
	
	/**
	 * 구글 스프레드 시트로 출력한다.
	 */
	public void publishing(String dt) {
		try {
			log.info("{}일 전략 24 데이터 전송 시작", dt);
			
			Map<String, Object> param = new HashMap<>();
			param.put("SHEET_NM", "투자전략24");
			// 삭제 코드
			param.put("dmlCd", "D");
			// DML 코드를 'D'로 넘겨서 투자전략 20 시트를 초기화 한다.
			tactic000.callMacro(Utils.writeValueAsJson(param));
			
			// 특정 일자의 거래 내역 중 시가총액 순정렬하여 500개만 가져온다.
			List<ItmTrd> itmTrds = itmTrdRepo.findByDtQuery(Utils.dateParse(dt), PageRequest.of(0, 500, Sort.by("mktTotAmt").ascending()));
			
			List<TacticVO> tactics = new ArrayList<>();
			
			Map<String, Object> rank = new HashMap<>();
			
			rank.put("TOT_AMT_RANK", 1L);
			rank.put("TOT_AMT_SAME_RANK", 0L);
			rank.put("MKT_TOT_AMT", null);
			rank.put("PBR_RANK", 1L);
			rank.put("PBR_SAME_RANK", 0L);
			rank.put("PBR", null);
			rank.put("PCR_RANK", 1L);
			rank.put("PCR_SAME_RANK", 0L);
			rank.put("PCR", null);
			rank.put("PER_RANK", 1L);
			rank.put("PER_SAME_RANK", 0L);
			rank.put("PER", null);
			rank.put("PSR_RANK", 1L);
			rank.put("PSR_SAME_RANK", 0L);
			rank.put("PSR", null);
			
			itmTrds.stream().forEach(itmTrd -> {
				TacticVO tactic = new TacticVO();
				
				Optional<Itm> itm = itmRepo.findByItmCd(itmTrd.getItmCd());
				
				tactic.setItmCd(itmTrd.getItmCd());
				tactic.setItmNm(itm.get().getItmNm());
				tactic.setMkt(itm.get().getMkt());
				tactic.setEdAmt(itmTrd.getEdAmt());
				tactic.setMktTotAmt(itmTrd.getMktTotAmt());
				tactic.setPbr(itmTrd.getPbr());
				tactic.setPcr(itmTrd.getPcr());
				tactic.setPer(itmTrd.getPer());
				tactic.setPsr(itmTrd.getPsr());
				tactic.setDt(dt);
				tactic.setMinEdAmt(itmTrdRepo.findMinEdAmtByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), Utils.dateParse(dt)));
				tactic.setMaxEdAmt(itmTrdRepo.findMaxEdAmtByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), Utils.dateParse(dt)));
				
				// 매수목표가 생성((365일 최고가 / 150) * 100 = 매수목표가로 구매 후 365일 최고가가 되면 50% 상승)
				tactic.setTgEdAmt(Utils.multiply(Utils.divide(tactic.getMaxEdAmt(), new BigDecimal("150"), 0), new BigDecimal("100"), 0));
				
				// 이전 시가총액이 없을 경우
				if(rank.get("MKT_TOT_AMT") == null) {
					// 시가총액을 Map에 담는다.
					rank.put("MKT_TOT_AMT", tactic.getMktTotAmt());
					// 순위를 그냥 가져온다.
					tactic.setTotAmtRank((Long)rank.get("TOT_AMT_RANK"));
					
				// 이전 시가총액과 현재시가총액이 같을 경우
				} else if(tactic.getMktTotAmt().compareTo((BigDecimal)rank.get("MKT_TOT_AMT")) == 0) {
					// 시가총액을 Map에 담는다.
					rank.put("MKT_TOT_AMT", tactic.getMktTotAmt());
					// 동일 순위 값을 1증가 시킨다.
					rank.put("TOT_AMT_SAME_RANK", (Long)rank.get("TOT_AMT_SAME_RANK") + 1);
					// 순위를 그냥 가져온다.
					tactic.setTotAmtRank((Long)rank.get("TOT_AMT_RANK"));
					
				// 그 외의 경우(시가총액으로 정렬한 데이터이므로 증가값임)
				} else {
					// 순위에 동일 순위 값을 더한다.
					rank.put("TOT_AMT_RANK", (Long)rank.get("TOT_AMT_RANK") + (Long)rank.get("TOT_AMT_SAME_RANK"));
					// 순위를 증가 한다.
					rank.put("TOT_AMT_RANK", (Long)rank.get("TOT_AMT_RANK") + 1);
					// 시가총액을 Map에 담는다.
					rank.put("MKT_TOT_AMT", tactic.getMktTotAmt());
					// 중가된 순위를 가져온다.
					tactic.setTotAmtRank((Long)rank.get("TOT_AMT_RANK"));
				}
				
				tactics.add(tactic);
			});
			
			// PBR로 정렬하여 Loop
			tactics.stream()
					.sorted(Comparator.comparing(TacticVO::getPbr))
					.forEach(tactic -> {
						// 이전 PBR이 없을 경우
						if(rank.get("PBR") == null) {
							// PBR을 Map에 담는다.
							rank.put("PBR", tactic.getPbr());
							// 순위를 그냥 가져온다.
							tactic.setPbrRank((Long)rank.get("PBR_RANK"));
							
							// 이전 PBR과 현재시가총액이 같을 경우
						} else if(tactic.getPbr().compareTo((BigDecimal)rank.get("PBR")) == 0) {
							// PBR을 Map에 담는다.
							rank.put("PBR", tactic.getPbr());
							// 동일 순위 값을 1증가 시킨다.
							rank.put("PBR_SAME_RANK", (Long)rank.get("PBR_SAME_RANK") + 1);
							// 순위를 그냥 가져온다.
							tactic.setPbrRank((Long)rank.get("PBR_RANK"));
							
							// 그 외의 경우(PBR로 정렬한 데이터이므로 증가값임)
						} else {
							// PBR을 Map에 담는다.
							rank.put("PBR", tactic.getPbr());
							// 순위에 동일 순위 값을 더한다.
							rank.put("PBR_RANK", (Long)rank.get("PBR_RANK") + (Long)rank.get("PBR_SAME_RANK"));
							// 동일 순위 값을 초기화 한다.
							rank.put("PBR_SAME_RANK", 0L);
							// 순위를 증가 한다.
							rank.put("PBR_RANK", (Long)rank.get("PBR_RANK") + 1);
							// 중가된 순위를 가져온다.
							tactic.setPbrRank((Long)rank.get("PBR_RANK"));
						}
					});
			
			// PCR로 정렬하여 Loop
			tactics.stream()
					.sorted(Comparator.comparing(TacticVO::getPcr))
					.forEach(tactic -> {
						// 이전 PCR이 없을 경우
						if(rank.get("PCR") == null) {
							// PCR을 Map에 담는다.
							rank.put("PCR", tactic.getPcr());
							// 순위를 그냥 가져온다.
							tactic.setPcrRank((Long)rank.get("PCR_RANK"));
							
							// 이전 PCR과 현재시가총액이 같을 경우
						} else if(tactic.getPcr().compareTo((BigDecimal)rank.get("PCR")) == 0) {
							// PCR을 Map에 담는다.
							rank.put("PCR", tactic.getPcr());
							// 동일 순위 값을 1증가 시킨다.
							rank.put("PCR_SAME_RANK", (Long)rank.get("PCR_SAME_RANK") + 1);
							// 순위를 그냥 가져온다.
							tactic.setPcrRank((Long)rank.get("PCR_RANK"));
							
							// 그 외의 경우(PCR로 정렬한 데이터이므로 증가값임)
						} else {
							// PCR을 Map에 담는다.
							rank.put("PCR", tactic.getPcr());
							// 순위에 동일 순위 값을 더한다.
							rank.put("PCR_RANK", (Long)rank.get("PCR_RANK") + (Long)rank.get("PCR_SAME_RANK"));
							// 동일 순위 값을 초기화 한다.
							rank.put("PCR_SAME_RANK", 0L);
							// 순위를 증가 한다.
							rank.put("PCR_RANK", (Long)rank.get("PCR_RANK") + 1);
							// 중가된 순위를 가져온다.
							tactic.setPcrRank((Long)rank.get("PCR_RANK"));
						}
					});
			
			// PER로 정렬하여 Loop
			tactics.stream()
					.sorted(Comparator.comparing(TacticVO::getPer))
					.forEach(tactic -> {
						// 이전 PER이 없을 경우
						if(rank.get("PER") == null) {
							// PER을 Map에 담는다.
							rank.put("PER", tactic.getPer());
							// 순위를 그냥 가져온다.
							tactic.setPerRank((Long)rank.get("PER_RANK"));
							
							// 이전 PER과 현재시가총액이 같을 경우
						} else if(tactic.getPer().compareTo((BigDecimal)rank.get("PER")) == 0) {
							// PER을 Map에 담는다.
							rank.put("PER", tactic.getPer());
							// 동일 순위 값을 1증가 시킨다.
							rank.put("PER_SAME_RANK", (Long)rank.get("PER_SAME_RANK") + 1);
							// 순위를 그냥 가져온다.
							tactic.setPerRank((Long)rank.get("PER_RANK"));
							
							// 그 외의 경우(PER로 정렬한 데이터이므로 증가값임)
						} else {
							// PCR을 Map에 담는다.
							rank.put("PER", tactic.getPer());
							// 순위에 동일 순위 값을 더한다.
							rank.put("PER_RANK", (Long)rank.get("PER_RANK") + (Long)rank.get("PER_SAME_RANK"));
							// 동일 순위 값을 초기화 한다.
							rank.put("PER_SAME_RANK", 0L);
							// 순위를 증가 한다.
							rank.put("PER_RANK", (Long)rank.get("PER_RANK") + 1);
							// 중가된 순위를 가져온다.
							tactic.setPerRank((Long)rank.get("PER_RANK"));
						}
					});
			
			// PSR로 정렬하여 Loop
			tactics.stream()
					.sorted(Comparator.comparing(TacticVO::getPsr))
					.forEach(tactic -> {
						// 이전 PSR이 없을 경우
						if(rank.get("PSR") == null) {
							// PSR을 Map에 담는다.
							rank.put("PSR", tactic.getPsr());
							// 순위를 그냥 가져온다.
							tactic.setPsrRank((Long)rank.get("PSR_RANK"));
							
							// 이전 PSR과 현재 PSR이 같을 경우
						} else if(tactic.getPsr().compareTo((BigDecimal)rank.get("PSR")) == 0) {
							// PSR을 Map에 담는다.
							rank.put("PSR", tactic.getPsr());
							// 동일 순위 값을 1증가 시킨다.
							rank.put("PSR_SAME_RANK", (Long)rank.get("PSR_SAME_RANK") + 1);
							// 순위를 그냥 가져온다.
							tactic.setPsrRank((Long)rank.get("PSR_RANK"));
							
							// 그 외의 경우(PSR로 정렬한 데이터이므로 증가값임)
						} else {
							// PSR을 Map에 담는다.
							rank.put("PSR", tactic.getPsr());
							// 순위에 동일 순위 값을 더한다.
							rank.put("PSR_RANK", (Long)rank.get("PSR_RANK") + (Long)rank.get("PSR_SAME_RANK"));
							// 동일 순위 값을 초기화 한다.
							rank.put("PSR_SAME_RANK", 0L);
							// 순위를 증가 한다.
							rank.put("PSR_RANK", (Long)rank.get("PSR_RANK") + 1);
							// 중가된 순위를 가져온다.
							tactic.setPsrRank((Long)rank.get("PSR_RANK"));
						}
					});
			
			tactics.stream().forEach(tactic -> {
				tactic.setTotRank(tactic.getTotAmtRank() + tactic.getPbrRank() + tactic.getPcrRank() + tactic.getPerRank() + tactic.getPsrRank());
			});
			
			param.remove("dmlCd");
			// 입력 코드
			param.put("dmlCd", "I");
			// 데이터
			param.put("data", tactics.stream().sorted(Comparator.comparing(TacticVO::getTotRank)).collect(Collectors.toList()));
			
			// 새로운 데이터를 넘긴다.
			tactic000.callMacro(Utils.writeValueAsJson(param));
			
			log.info("{}일 전략 24 데이터 전송 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
}
