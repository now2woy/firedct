package ji.hs.firedct.tactic.svc;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.itm.dao.ItmFincSts;
import ji.hs.firedct.itm.dao.ItmFincStsRepository;
import ji.hs.firedct.itm.dao.ItmTrd;
import ji.hs.firedct.itm.dao.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 전략 20을 구글 스프레드 시트로 출력한다.
 * @author now2woy
 *
 */
@Slf4j
@Service
public class Tactic020 {
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	@Autowired
	private ItmFincStsRepository itmFincStsRepo;
	
	@Autowired
	private Tactic000 tactic000;
	
	/**
	 * 구글 스프레드 시트로 출력한다.
	 */
	public void publishing(String dt) {
		try {
			log.info("{}일 전략 20 데이터 전송 시작", dt);
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> param = new HashMap<>();
			param.put("SHEET_NM", "투자전략20");
			// 삭제 코드
			param.put("dmlCd", "D");
			
			// DML 코드를 'D'로 넘겨서 투자전략 20 시트를 초기화 한다.
			tactic000.callMacro(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(param));
			
			param.remove("dmlCd");
			// 입력 코드
			param.put("dmlCd", "I");
			
			final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			final Date parseDt = format.parse(dt);
			
			List<ItmTrd> itmTrds = itmTrdRepo.findByDt(parseDt, Sort.by("pbr").ascending().and(Sort.by("per").ascending().and(Sort.by("mktTotAmt").ascending())));
			
			itmTrds.stream().forEach(itmTrd -> {
				itmTrd.setDtStr(format.format(itmTrd.getDt()));
				itmTrd.setItmNm(itmTrd.getItm().getItmNm());
				itmTrd.setMinEdAmt(itmTrdRepo.findMinEdAmtByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), parseDt));
				itmTrd.setMaxEdAmt(itmTrdRepo.findMaxEdAmtByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), parseDt));
				
				// 매수목표가 생성((365일 최고가 / 150) * 100 = 매수목표가로 구매 후 365일 최고가가 되면 50% 상승)
				itmTrd.setTgEdAmt(Utils.divide(itmTrd.getMaxEdAmt(), new BigDecimal("150"), 0).multiply(new BigDecimal("100")));
				List<ItmFincSts> itmFincSts = itmFincStsRepo.findByItmCdAndStdDtLessThanEqual(itmTrd.getItmCd(), parseDt, PageRequest.of(0, 1, Sort.by("stdDt").descending()));
				
				if(!itmFincSts.isEmpty()) {
					itmTrd.setRoe(itmFincSts.get(0).getRoe());
					itmTrd.setRoa(itmFincSts.get(0).getRoa());
					itmTrd.setDebtRt(itmFincSts.get(0).getDebtRt());
				}
				
			});
			
			itmTrds = itmTrds.stream()
					.filter(itmTrd -> itmTrd.getDebtRt() != null)
					.filter(itmTrd -> itmTrd.getDebtRt().compareTo(new BigDecimal("50")) <= 0)
					.filter(itmTrd -> itmTrd.getRoa() != null)
					.filter(itmTrd -> itmTrd.getRoa().compareTo(new BigDecimal("5")) >= 0)
					.filter(itmTrd -> itmTrd.getPbr() != null)
					.filter(itmTrd -> itmTrd.getPbr().compareTo(new BigDecimal("0.2")) >= 0)
					.collect(Collectors.toList());
			
			param.put("data", itmTrds);
			
			// 새로운 데이터를 넘긴다.
			tactic000.callMacro(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(param));
			
			log.info("{}일 전략 20 데이터 전송 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
}