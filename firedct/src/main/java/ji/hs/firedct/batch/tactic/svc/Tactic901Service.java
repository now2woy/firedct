package ji.hs.firedct.batch.tactic.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.batch.tactic.dao.TacticVO;
import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.ItmTrd;
import ji.hs.firedct.data.stock.repository.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 30일간 거래회전율 액셀로 전송한다.
 * @author now2woy
 *
 */
@Slf4j
@Service
public class Tactic901Service {
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	@Autowired
	private Tactic000Service tactic000Service;
	
	/**
	 * 
	 * @param dt
	 */
	public void publishing(String dt) {
		log.info("{}일 거래회전율 데이터 전송 시작", dt);
		
		List<ItmTrd> itmTrds = itmTrdRepo.findByDt(Utils.dateParse(dt), Sort.by("itmCd").ascending());
		
		if(itmTrds.isEmpty()) {
			log.info("조회된 데이터가 없어 구글 스프레드 시트로 데이터 전송 생략");
		} else {
			Map<String, Object> param = new HashMap<>();
			param.put("SHEET_NM", "거래회전율");
			
			// 삭제 코드
			param.put("dmlCd", "D");
			
			// DML 코드를 'D'로 넘겨서 투자전략 20 시트를 초기화 한다.
			tactic000Service.callMacro("1", Utils.writeValueAsJson(param));
			tactic000Service.callMacro("2", Utils.writeValueAsJson(param));
			
			List<TacticVO> tactics = new ArrayList<>();
			
			itmTrds.stream().forEach(itmTrd -> {
				AtomicInteger i = new AtomicInteger(1);
				TacticVO tactic = new TacticVO();
				
				tactic.setDt(dt);
				tactic.setItmCd(itmTrd.getItmCd());
				
				List<ItmTrd> temps = itmTrdRepo.findByItmCdAndDtLessThanEqual(itmTrd.getItmCd(), Utils.dateParse(dt), PageRequest.of(0, 30, Sort.by("dt").descending()));
				
				temps.stream().forEach(temp -> {
					switch (i.getAndIncrement()) {
					case 1:
						tactic.setVal01(temp.getTrdTnovRt());
						break;
					case 2:
						tactic.setVal02(temp.getTrdTnovRt());
						break;
					case 3:
						tactic.setVal03(temp.getTrdTnovRt());
						break;
					case 4:
						tactic.setVal04(temp.getTrdTnovRt());
						break;
					case 5:
						tactic.setVal05(temp.getTrdTnovRt());
						break;
					case 6:
						tactic.setVal06(temp.getTrdTnovRt());
						break;
					case 7:
						tactic.setVal07(temp.getTrdTnovRt());
						break;
					case 8:
						tactic.setVal08(temp.getTrdTnovRt());
						break;
					case 9:
						tactic.setVal09(temp.getTrdTnovRt());
						break;
					case 10:
						tactic.setVal10(temp.getTrdTnovRt());
						break;
					case 11:
						tactic.setVal11(temp.getTrdTnovRt());
						break;
					case 12:
						tactic.setVal12(temp.getTrdTnovRt());
						break;
					case 13:
						tactic.setVal13(temp.getTrdTnovRt());
						break;
					case 14:
						tactic.setVal14(temp.getTrdTnovRt());
						break;
					case 15:
						tactic.setVal15(temp.getTrdTnovRt());
						break;
					case 16:
						tactic.setVal16(temp.getTrdTnovRt());
						break;
					case 17:
						tactic.setVal17(temp.getTrdTnovRt());
						break;
					case 18:
						tactic.setVal18(temp.getTrdTnovRt());
						break;
					case 19:
						tactic.setVal19(temp.getTrdTnovRt());
						break;
					case 20:
						tactic.setVal20(temp.getTrdTnovRt());
						break;
					case 21:
						tactic.setVal21(temp.getTrdTnovRt());
						break;
					case 22:
						tactic.setVal22(temp.getTrdTnovRt());
						break;
					case 23:
						tactic.setVal23(temp.getTrdTnovRt());
						break;
					case 24:
						tactic.setVal24(temp.getTrdTnovRt());
						break;
					case 25:
						tactic.setVal25(temp.getTrdTnovRt());
						break;
					case 26:
						tactic.setVal26(temp.getTrdTnovRt());
						break;
					case 27:
						tactic.setVal27(temp.getTrdTnovRt());
						break;
					case 28:
						tactic.setVal28(temp.getTrdTnovRt());
						break;
					case 29:
						tactic.setVal29(temp.getTrdTnovRt());
						break;
					case 30:
						tactic.setVal30(temp.getTrdTnovRt());
						break;
					default:
						break;
					}
				});
				
				tactics.add(tactic);
			});
			
			param.remove("dmlCd");
			
			// 입력 코드
			param.put("dmlCd", "I");
			
			// 데이터
			param.put("data", tactics);
			
			// 새로운 데이터를 넘긴다.
			tactic000Service.callMacro("1", Utils.writeValueAsJson(param));
			tactic000Service.callMacro("2", Utils.writeValueAsJson(param));
		}
		
		log.info("{}일 거래회전율 데이터 전송 종료", dt);
	}
}
