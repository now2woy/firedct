package ji.hs.firedct.batch.backtest.svc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ji.hs.firedct.batch.backtest.dao.BackTestStsVO;
import ji.hs.firedct.batch.backtest.dao.BackTestTrdVO;
import ji.hs.firedct.batch.tactic.dao.TacticVO;
import ji.hs.firedct.batch.tactic.svc.Tactic000Service;
import ji.hs.firedct.batch.tactic.svc.Tactic020Service;
import ji.hs.firedct.batch.tactic.svc.Tactic024Service;
import ji.hs.firedct.batch.tactic.svc.Tactic030Service;
import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.ItmTrd;
import ji.hs.firedct.data.stock.repository.ItmFincStsRepository;
import ji.hs.firedct.data.stock.repository.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 전략 20으로 과거 데이터를 분석한 결과를 출력한다.
 * @author now2woy
 *
 */
@Slf4j
@Service
public class BackTest020Service {
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
	
	@Autowired
	private Tactic030Service tactic030Service;
	
	/**
	 * 구글 스프레드 시트로 출력한다.
	 */
	public void publishing(String tacticCd, String stDt, int term, String cd, BigDecimal dpsAmt, BigDecimal itmQty, BigDecimal fee, BigDecimal tax) {
		try {
			log.info("{}일부터 {}단위 백테스트 시작", stDt, term);
			
			createData(tacticCd, stDt, term, cd, dpsAmt, itmQty, fee, tax);
			
			log.info("{}일부터 {}단위 백테스트 종료", stDt, term);
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 
	 * @param yr
	 * @param qt
	 * @param cd
	 */
	public void createData(String tacticCd, String stDt, int term, String cd, BigDecimal dpsAmt, BigDecimal itmQty, BigDecimal fee, BigDecimal tax) {
		List<BackTestTrdVO> backTestTrds = new ArrayList<>();
		List<BackTestStsVO> backTestStss = getDateList(stDt, term);
		
		Map<String, BigDecimal> data = new HashMap<>();
		data.put("FWD_AMT", BigDecimal.ZERO);
		
		BackTestStsVO result = new BackTestStsVO();
		
		// 회차만큼 반복
		backTestStss.stream().forEach(backTestSts ->{
			
			log.info("{}, {}, {}", backTestSts.getSeq(), backTestSts.getBuyDt(), backTestSts.getSellDt());
			
			backTestSts.setTactic(tacticCd);
			backTestSts.setFwdAmt(data.get("FWD_AMT"));
			backTestSts.setDpsAmt(dpsAmt);
			backTestSts.setItmQty(itmQty);
			backTestSts.setItmAsignAmt(Utils.divide(Utils.add(backTestSts.getDpsAmt(), backTestSts.getFwdAmt()), backTestSts.getItmQty(), 0));
			
			// Tactic020Service를 이용하여 매수일 기준 데이터를 가져온다.
			List<TacticVO> tactics = null;
			
			
			if("20-1".equals(tacticCd)) {
				tactics = tactic020Service.createTacticDataVer001(backTestSts.getBuyDt());
			} else if("20-2".equals(tacticCd)) {
				tactics = tactic020Service.createTacticDataVer002(backTestSts.getBuyDt());
			} else if("24-1".equals(tacticCd)) {
				tactics = tactic024Service.createTacticDataVer001(backTestSts.getBuyDt());
			} else if("24-2".equals(tacticCd)) {
				tactics = tactic024Service.createTacticDataVer002(backTestSts.getBuyDt());
			} else if("30-1".equals(tacticCd)) {
				tactics = tactic030Service.createTacticDataVer001(backTestSts.getBuyDt());
			}
			
			if(tactics != null) {
				if(!tactics.isEmpty()) {
					// 가져온 데이터에서 상위 20개만 남긴다.
					tactics = tactics.stream().filter(tactic -> tactic.getEdAmt().compareTo(backTestSts.getItmAsignAmt()) <= 0)
							.limit(itmQty.longValue())
							.collect(Collectors.toList());
					
					tactics.stream().forEach(tactic -> {
						BackTestTrdVO backTestTrdVO = new BackTestTrdVO();
						
						backTestTrdVO.setTactic(tacticCd);
						backTestTrdVO.setItmCd(tactic.getItmCd());
						backTestTrdVO.setItmNm(tactic.getItmNm());
						backTestTrdVO.setBuyDt(backTestSts.getBuyDt());
						backTestTrdVO.setBuyPrc(tactic.getEdAmt());
						backTestTrdVO.setBuyQty(Utils.divide(backTestSts.getItmAsignAmt(), backTestTrdVO.getBuyPrc(), 0, RoundingMode.DOWN));
						backTestTrdVO.setSellQty(backTestTrdVO.getBuyQty());
						backTestTrdVO.setBuyAmt(Utils.multiply(backTestTrdVO.getBuyPrc(), backTestTrdVO.getBuyQty(), 0));
						backTestTrdVO.setSellDt(backTestSts.getSellDt());
						
						Optional<ItmTrd> itmTrd = itmTrdRepo.findByItmCdAndDt(tactic.getItmCd(), Utils.dateParse(backTestSts.getSellDt()));
						
						if(itmTrd.isPresent()) {
							backTestTrdVO.setSellPrc(itmTrd.get().getEdAmt());
							backTestTrdVO.setSellAmt(Utils.multiply(backTestTrdVO.getSellPrc(), backTestTrdVO.getSellQty(), 0));
						}
						
						backTestTrdVO.setIncmAmt(Utils.subtract(backTestTrdVO.getSellAmt(), backTestTrdVO.getBuyAmt()));
						
						backTestSts.setBuyAmt(Utils.add(backTestSts.getBuyAmt(), backTestTrdVO.getBuyAmt()));
						backTestSts.setSellAmt(Utils.add(backTestSts.getSellAmt(), backTestTrdVO.getSellAmt()));
						
						log.info("{}, {}", backTestTrdVO.toString());
						
						
						backTestTrds.add(backTestTrdVO);
					});
					
					// 매수잔액 = (입금액 + 이월금액) - 매수금액
					backTestSts.setBuyBlncAmt(Utils.subtract(Utils.add(backTestSts.getDpsAmt(), backTestSts.getFwdAmt()), backTestSts.getBuyAmt()));
					
					// 매도잔액 = 매수잔액 + 매도금액
					backTestSts.setSellBlncAmt(Utils.add(backTestSts.getBuyBlncAmt(), backTestSts.getSellAmt()));
					
					// 손액금액 = 매도잔액 - (입금액 + 이월금액)
					backTestSts.setIncmAmt(Utils.subtract(backTestSts.getSellBlncAmt(), Utils.add(backTestSts.getDpsAmt(), backTestSts.getFwdAmt())));
					
					// 손액률 = (손액금액 / (입금액 + 이월금액)) * 100
					backTestSts.setIncmRt(Utils.multiply(Utils.divide(backTestSts.getIncmAmt(), Utils.add(backTestSts.getDpsAmt(), backTestSts.getFwdAmt()), 10), new BigDecimal("100"), 2));
					
					// 이월금액 = 매도잔액
					data.put("FWD_AMT", backTestSts.getSellBlncAmt());
					
					// 결과입금액 = 결과입금액 + 입금액
					result.setDpsAmt(Utils.add(result.getDpsAmt(), backTestSts.getDpsAmt()));
					
					// 결과매도잔액 = 마지막 매도잔액
					result.setSellBlncAmt(backTestSts.getSellBlncAmt());
				}
			}
		});
		
		// 결과손익금액 = 결과매도잔액 - 결과입금액
		result.setIncmAmt(Utils.subtract(result.getSellBlncAmt(), result.getDpsAmt()));
		
		// 결과손익률 = (결과손익금액 / 결과입금액) * 100
		result.setIncmRt(Utils.multiply(Utils.divide(result.getIncmAmt(), result.getDpsAmt(), 10), new BigDecimal("100"), 2));
		
		backTestStss.add(result);
		
		Map<String, Object> param = new HashMap<>();
		param.put("SHEET_NM", "백테스트 집계");
		param.put("data", backTestStss);
		
		tactic000Service.callSheet(param, 1);
		
		param.put("SHEET_NM", "백테스트");
		param.put("data", backTestTrds);
		
		tactic000Service.callSheet(param, 1);
	}
	
	/**
	 * 분기 단위로 데이터를 가져올 수 있게 날자 목록을 가져온다.
	 * @param yr
	 * @param qt
	 * @param cd
	 * @return
	 */
	public List<BackTestStsVO> getDateList(final String dt, final int term){
		List<BackTestStsVO> results = new ArrayList<>();
		// 현재일을 가져온다.
		String thisDay = Utils.thisDay();
		String addDay = dt;
		AtomicInteger i = new AtomicInteger(1);
		
		// 현재일보다 작거나 같을 경우
		while(Integer.parseInt(Utils.addMonth(Utils.dateParse(addDay), term)) <= Integer.parseInt(thisDay)){
			Date stDt = null;
			Date edDt = null;
			
			// 시작일자를 가져온다.
			stDt = itmTrdRepo.findMinDtByDt(Utils.dateParse(addDay));
			
			if(!itmFincStsRepo.findByStdDtLessThanEqual(stDt, PageRequest.of(0, 1)).isEmpty()) {
				// 날자를 기간만큼 증가 시킨다
				addDay = Utils.addMonth(Utils.dateParse(addDay), term);
				
				// 종료일자를 가져온다.
				edDt = itmTrdRepo.findMinDtByDt(Utils.dateParse(addDay));
				
				if(stDt != null && edDt != null){
					BackTestStsVO backTestStsVO = new BackTestStsVO();
					
					backTestStsVO.setSeq(i.getAndIncrement());
					backTestStsVO.setBuyDt(Utils.dateFormat(stDt));
					backTestStsVO.setSellDt(Utils.dateFormat(edDt));
					
					results.add(backTestStsVO);
				}
			}
		}
		
		return results;
	}
}
