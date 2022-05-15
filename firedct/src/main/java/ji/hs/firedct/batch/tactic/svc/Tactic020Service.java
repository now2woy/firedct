package ji.hs.firedct.batch.tactic.svc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
 * 전략 20을 구글 스프레드 시트로 출력한다.
 * @author now2woy
 *
 */
@Slf4j
@Service
public class Tactic020Service {
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
			log.info("{}일 전략 20 데이터 전송 시작", dt);
			
			List<TacticVO> tactics = createTacticDataVer001(dt);
			
			if(tactics.isEmpty()) {
				log.info("조회된 데이터가 없어 구글 스프레드 시트로 데이터 전송 생략");
			} else {
				
				// 1번 시트로 데이터 전송
				Map<String, Object> param = new HashMap<>();
				param.put("SHEET_NM", "전략20-1");
				param.put("data", tactics);
				tactic000Service.callSheet(param, 0);
				
				// 2번 시트로 데이터 전송
				param.put("SHEET_NM", "전략20-2");
				param.put("data", createTacticDataVer002(tactics));
				tactic000Service.callSheet(param, 0);
			}
			
			log.info("{}일 전략 20 데이터 전송 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 전략 20의 데이터를 생성한다.
	 * @param dt
	 * @return
	 */
	public List<TacticVO> createTacticDataVer001(String dt){
		List<TacticVO> tactics = new ArrayList<>();
		List<ItmFincSts> dates = itmFincStsRepo.findByStdDtLessThanEqual(Utils.dateParse(dt), PageRequest.of(0, 1, Sort.by("stdDt").descending()));
		Date stdDt = null;
		
		if(!dates.isEmpty()) {
			stdDt = dates.get(0).getStdDt();
		}
		
		if(stdDt != null) {
			List<ItmFincSts> itmFincStss = itmFincStsRepo.findByStdDtQuery(stdDt);
			
			itmFincStss.stream().forEach(itmFincSts -> {
				Optional<ItmTrd> itmTrd = itmTrdRepo.findByItmCdAndDt(itmFincSts.getItmCd(), Utils.dateParse(dt));
				
				if(itmTrd.isPresent()) {
					TacticVO tactic = new TacticVO();
					
					Optional<Itm> itm = itmRepo.findByItmCd(itmFincSts.getItmCd());
					
					tactic.setItmCd(itmFincSts.getItmCd());
					tactic.setItmNm(itm.get().getItmNm());
					tactic.setMkt(itm.get().getMkt());
					tactic.setEdAmt(itmTrd.get().getEdAmt());
					tactic.setMktTotAmt(itmTrd.get().getMktTotAmt());
					tactic.setPbr(itmTrd.get().getPbr());
					tactic.setPcr(itmTrd.get().getPcr());
					tactic.setPer(itmTrd.get().getPer());
					tactic.setPsr(itmTrd.get().getPsr());
					tactic.setDt(dt);
					tactic.setRoe(itmFincSts.getRoe());
					tactic.setRoa(itmFincSts.getRoa());
					tactic.setDebtRt(itmFincSts.getDebtRt());
					tactic.setFscrFst(itmFincSts.getFscrFst());
					tactic.setFscrSnd(itmFincSts.getFscrSnd());
					tactic.setFscrTrd(itmFincSts.getFscrTrd());
					tactic.setMinEdAmt(itmTrdRepo.findMinEdAmtByItmCdAndDtGreaterThanEqual365(itmFincSts.getItmCd(), Utils.dateParse(dt)));
					tactic.setMaxEdAmt(itmTrdRepo.findMaxEdAmtByItmCdAndDtGreaterThanEqual365(itmFincSts.getItmCd(), Utils.dateParse(dt)));
					
					// 매수목표가 생성((365일 최고가 / 150) * 100 = 매수목표가로 구매 후 365일 최고가가 되면 50% 상승)
					tactic.setTgEdAmt(Utils.multiply(Utils.divide(tactic.getMaxEdAmt(), new BigDecimal("150"), 0), new BigDecimal("100"), 0));
					
					tactics.add(tactic);
				}
			});
		}
		
		return filter(tactics);
	}
	
	/**
	 * PBR, PCR, PER, PSR 순위 합계로 정렬
	 * @param dt
	 * @return
	 */
	public List<TacticVO> createTacticDataVer002(String dt) {
		List<TacticVO> tactics = createTacticDataVer001(dt);
		
		return createTacticDataVer002(tactics);
	}
	
	/**
	 * PBR로 필터 후 PBR ASC, ROA DESC, DEBT_RT ASC 로 정렬
	 * @param tactics
	 * @return
	 */
	private List<TacticVO> filter(List<TacticVO> tactics){
		var list = tactics.stream()
				.filter(tactic -> tactic.getPbr() != null && tactic.getPbr().compareTo(new BigDecimal("0.2")) >= 0)
				.sorted(Comparator.comparing(TacticVO::getPbr).reversed()
						.thenComparing(TacticVO::getRoa)
						.thenComparing(TacticVO::getDebtRt).reversed())
				.collect(Collectors.toList());
		
		AtomicInteger i = new AtomicInteger(1);
		
		list.stream().forEach(tactic -> {
			tactic.setSeq(i.getAndIncrement());
		});
		
		return list;
	}
	
	/**
	 * F-Score 1, 2, 3의 값이 1인것만 추출
	 * @param tactics
	 * @return
	 */
	private List<TacticVO> createTacticDataVer002(List<TacticVO> tactics){
		var list = tactics.stream()
				.filter(tactic -> "1".equals(tactic.getFscrFst()))
				.filter(tactic -> "1".equals(tactic.getFscrSnd()))
				.filter(tactic -> "1".equals(tactic.getFscrTrd()))
				.collect(Collectors.toList());
		
		AtomicInteger i = new AtomicInteger(1);
		
		list.stream().forEach(tactic -> {
			tactic.setSeq(i.getAndIncrement());
		});
		
		return list;
	}
}