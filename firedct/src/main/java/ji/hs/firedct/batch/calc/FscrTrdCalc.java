package ji.hs.firedct.batch.calc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.ItmFincSts;

/**
 * F-SCORE 3번 계산기
 * @author now2woy
 *
 */
public class FscrTrdCalc {
	/**
	 * F-SCORE 3번 계산
	 * @param yr
	 * @param qt
	 * @param itmCd
	 * @return
	 */
	public static String calc(final List<ItmFincSts> itmFincStss) {
		String result = null;
		var oprCsflw = new HashMap<String, BigDecimal>();
		oprCsflw.put("OPR_CSFLW", null);
		
		// 4개 분기 자료를 가져올 수 있을때 계산
		if(itmFincStss.size() == 4) {
			itmFincStss.stream().forEach(itmFincSts -> {
				// 값이 있을 경우 더한다.
				if(itmFincSts.getOprCsflw() != null) {
					oprCsflw.put("OPR_CSFLW", Utils.add(oprCsflw.get("OPR_CSFLW"), itmFincSts.getOprCsflw()));
				}
			});
			
			// null이 아닐 경우
			if(oprCsflw.get("OPR_CSFLW") != null) {
				// 생성
				if(oprCsflw.get("OPR_CSFLW").compareTo(BigDecimal.ZERO) == 1) {
					result = "1";
				}else {
					result = "0";
				}
			}
		}
		
		return result;
	}
}
