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
 * 전략 30을 구글 스프레드 시트로 출력한다.
 * @author now2woy
 *
 */
@Slf4j
@Service
public class Tactic030Service {
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
	 * @param dt
	 */
	public void publishing(String dt) {
		log.info("{}일 전략 30 데이터 전송 시작", dt);
		
		List<TacticVO> tactics = createTacticDataVer001(dt);
		
		// 1번 시트로 데이터 전송
		Map<String, Object> param = new HashMap<>();
		param.put("SHEET_NM", "전략30-1");
		param.put("data", tactics);
		callSheet(param);
		
		log.info("{}일 전략 30 데이터 전송 종료", dt);
	}
	
	/**
	 * 
	 * @param dt
	 * @return
	 */
	public List<TacticVO> createTacticDataVer001(String dt){
		List<ItmTrd> itmTrds = itmTrdRepo.findByDt(Utils.dateParse(dt), Sort.by("mktTotAmt").ascending());
		
		// 500개만 짤라서 처리
		itmTrds = itmTrds.stream().limit(500).collect(Collectors.toList());
		
		List<TacticVO> tactics = new ArrayList<>();
		
		AtomicInteger i = new AtomicInteger(1);
		
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
			
			// 생산 지표가 없을 경우 제외
			if(tactic.getPbr() != null
			&& tactic.getPer() != null
			&& tactic.getRoe() != null
			&& tactic.getRoa() != null
			&& tactic.getDebtRt() != null) {
				// PBR 1 이상 제외
				if(tactic.getPbr().compareTo(new BigDecimal("1")) == -1) {
					
					// 스팩은 제외
					if(!"Y".equals(itm.get().getSpacYn())) {
						// F-Score 가 1일 경우만 List에 담는다.
						if("1".equals(tactic.getFscrFst())
								&& "1".equals(tactic.getFscrSnd())
								&& "1".equals(tactic.getFscrTrd())) {
							tactic.setSeq(i.getAndIncrement());
							tactics.add(tactic);
						}
					}
				}
			}
		});
		
		return sort(tactics);
	}
	
	/**
	 * 정렬
	 * @param tactics
	 * @return
	 */
	private List<TacticVO> sort(List<TacticVO> tactics) {
		var list = tactics.stream().sorted(Comparator.comparing(TacticVO::getPbr).thenComparing(TacticVO::getMktTotAmt)).collect(Collectors.toList());
		
		AtomicInteger i = new AtomicInteger(1);
		
		list.stream().forEach(tactic -> {
			tactic.setSeq(i.getAndIncrement());
		});
		
		return list;
	}
	
	/**
	 * 넘어온 데이터를 시트로 출력한다.
	 * @param param
	 * @param dmlCd
	 */
	private void callSheet(Map<String, Object> param) {
		// 삭제 dmlCd를 파라미터에 담는다.
		param.put("dmlCd", "D");
		
		// 시트에 데이터를 출력 한다.
		tactic000Service.callMacro("1", Utils.writeValueAsJson(param));
		
		// 입력 dmlCd를 파라미터에 담는다.
		param.put("dmlCd", "I");
		
		// 시트에 데이터를 출력 한다.
		tactic000Service.callMacro("1", Utils.writeValueAsJson(param));
	}
}
