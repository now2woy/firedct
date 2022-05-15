package ji.hs.firedct.batch.tactic.svc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.batch.tactic.dao.TacticVO;
import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.Itm;
import ji.hs.firedct.data.stock.entity.ItmFincSts;
import ji.hs.firedct.data.stock.entity.ItmTrd;
import ji.hs.firedct.data.stock.repository.ItmFincStsRepository;
import ji.hs.firedct.data.stock.repository.ItmRepository;
import ji.hs.firedct.data.stock.repository.ItmTrdRepository;
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
	private ItmFincStsRepository itmFincStsRepo;
	
	@Autowired
	private Tactic000Service tactic000Service;
	
	/**
	 * 구글 스프레드 시트로 출력한다.
	 */
	public void publishing(String dt) {
		try {
			log.info("{}일 전략 24 데이터 전송 시작", dt);
			
			List<TacticVO> tactics = createTacticData(dt);
			
			if(tactics.isEmpty()) {
				log.info("조회된 데이터가 없어 구글 스프레드 시트로 데이터 전송 생략");
			} else {
				// 1번 시트로 데이터 전송
				Map<String, Object> param = new HashMap<>();
				param.put("SHEET_NM", "전략24-1");
				param.put("data", createTacticDataVer001(tactics));
				tactic000Service.callSheet(param, 0);
				
				// 2번 시트로 데이터 전송
				param.put("SHEET_NM", "전략24-2");
				param.put("data", createTacticDataVer002(tactics));
				tactic000Service.callSheet(param, 0);
			}
			
			log.info("{}일 전략 24 데이터 전송 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 시가총액, PBR, PCR, PER, PSR 순위 합계로 정렬
	 * @param dt
	 * @return
	 */
	public List<TacticVO> createTacticDataVer001(String dt) {
		List<TacticVO> tactics = createTacticData(dt);
		
		return createTacticDataVer001(tactics);
	}
	
	/**
	 * PBR, PCR, PER, PSR 순위 합계로 정렬
	 * @param dt
	 * @return
	 */
	public List<TacticVO> createTacticDataVer002(String dt) {
		List<TacticVO> tactics = createTacticData(dt);
		
		return createTacticDataVer002(tactics);
	}
	
	/**
	 * 시가총액, PBR, PCR, PER, PSR 순위 합계로 정렬
	 * @param tactics
	 * @return
	 */
	private List<TacticVO> createTacticDataVer001(List<TacticVO> tactics){
		tactics.stream().forEach(tactic -> {
			tactic.setTotRank(tactic.getTotAmtRank() + tactic.getPbrRank() + tactic.getPcrRank() + tactic.getPerRank() + tactic.getPsrRank());
		});
		
		return sortTotRank(tactics);
	}
	
	/**
	 * PBR, PCR, PER, PSR 순위 합계로 정렬
	 * @param tactics
	 * @return
	 */
	private List<TacticVO> createTacticDataVer002(List<TacticVO> tactics){
		tactics.stream().forEach(tactic -> {
			tactic.setTotRank(tactic.getPbrRank() + tactic.getPcrRank() + tactic.getPerRank() + tactic.getPsrRank());
		});
		
		return sortTotRank(tactics);
	}
	
	/**
	 * 합계순위로 정렬
	 * @param tactics
	 * @return
	 */
	private List<TacticVO> sortTotRank(List<TacticVO> tactics) {
		AtomicInteger i = new AtomicInteger(1);
		
		tactics.stream().sorted(Comparator.comparing(TacticVO::getTotRank))
		.forEach(tactic -> {
			tactic.setSeq(i.getAndIncrement());
		});
		
		return tactics.stream().sorted(Comparator.comparing(TacticVO::getTotRank)).collect(Collectors.toList());
	}
	
	/**
	 * 기본 정보 생성
	 * @param dt
	 * @return
	 */
	private List<TacticVO> baseData(String dt){
		// 특정 일자의 거래 내역 중 시가총액 순정렬하여 500개만 가져온다.
		List<ItmTrd> itmTrds = itmTrdRepo.findByDtQuery(Utils.dateParse(dt), PageRequest.of(0, 500, Sort.by("mktTotAmt").ascending()));
		
		List<TacticVO> tactics = new ArrayList<>();
		
		itmTrds.stream().forEach(itmTrd -> {
			TacticVO tactic = new TacticVO();
			
			Optional<Itm> itm = itmRepo.findByItmCd(itmTrd.getItmCd());
			List<ItmFincSts> itmFincStss = itmFincStsRepo.findByItmCdAndStdDtLessThanEqual(itmTrd.getItmCd(), Utils.dateParse(dt), PageRequest.of(0, 1, Sort.by("stdDt").descending()));
			
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
			
			if(!itmFincStss.isEmpty()) {
				tactic.setRoe(itmFincStss.get(0).getRoe());
				tactic.setRoa(itmFincStss.get(0).getRoa());
				tactic.setDebtRt(itmFincStss.get(0).getDebtRt());
				tactic.setFscrFst(itmFincStss.get(0).getFscrFst());
				tactic.setFscrSnd(itmFincStss.get(0).getFscrSnd());
				tactic.setFscrTrd(itmFincStss.get(0).getFscrTrd());
			}
			
			// 매수목표가 생성((365일 최고가 / 150) * 100 = 매수목표가로 구매 후 365일 최고가가 되면 50% 상승)
			tactic.setTgEdAmt(Utils.multiply(Utils.divide(tactic.getMaxEdAmt(), new BigDecimal("150"), 0), new BigDecimal("100"), 0));
			
			// 
			tactics.add(tactic);
		});
		
		return tactics;
	}
	
	/**
	 * F-Score 로 필터링
	 * @param tactics
	 * @return
	 */
	private List<TacticVO> filter(List<TacticVO> tactics){
		return tactics.stream()
				.filter(tactic -> "1".equals(tactic.getFscrFst()))
				.filter(tactic -> "1".equals(tactic.getFscrSnd()))
				.filter(tactic -> "1".equals(tactic.getFscrTrd()))
				.collect(Collectors.toList());
	}
	
	/**
	 * 전략정보 생성
	 * @param dt
	 * @return
	 */
	private List<TacticVO> createTacticData(String dt){
		List<TacticVO> tactics = baseData(dt);
		
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
		
		tactics.stream().forEach(tactic -> {
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
		});
		
		// PBR로 정렬하여 Loop
		tactics.stream().sorted(Comparator.comparing(TacticVO::getPbr))
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
		tactics.stream().sorted(Comparator.comparing(TacticVO::getPcr))
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
		tactics.stream().sorted(Comparator.comparing(TacticVO::getPer))
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
		tactics.stream().sorted(Comparator.comparing(TacticVO::getPsr))
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
		
		return tactics;
	}
}