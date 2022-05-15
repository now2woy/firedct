package ji.hs.firedct.batch.calc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.ItmFincSts;

/**
 * F-SCORE 2번 계산기
 * @author now2woy
 *
 */
public class FscrSndCalc {
	/**
	 * F-SCORE 2번 계산
	 * @param yr
	 * @param qt
	 * @param itmCd
	 * @return
	 */
	public static String calc(final List<ItmFincSts> itmFincStss) {
		String result = null;
		var tsNetIncmAmt = new HashMap<String, BigDecimal>();
		tsNetIncmAmt.put("TS_NET_INCM_AMT", null);
		tsNetIncmAmt.put("OWN_TS_NET_INCM_AMT", null);
		
		// 4개 분기 자료를 가져올 수 있을때 계산
		if(itmFincStss.size() == 4) {
			itmFincStss.stream().forEach(itmFincSts -> {
				// 값이 있을 경우 더한다.
				if(itmFincSts.getTsNetIncmAmt() != null) {
					tsNetIncmAmt.put("TS_NET_INCM_AMT", Utils.add(tsNetIncmAmt.get("TS_NET_INCM_AMT"), itmFincSts.getTsNetIncmAmt()));
				}
				
				// 값이 있을 경우 더한다.
				if(itmFincSts.getOwnTsNetIncmAmt() != null) {
					tsNetIncmAmt.put("OWN_TS_NET_INCM_AMT", Utils.add(tsNetIncmAmt.get("OWN_TS_NET_INCM_AMT"), itmFincSts.getOwnTsNetIncmAmt()));
				}
			});
			
			// 지배주주 당기순이익 합계가 있을 경우
			if(tsNetIncmAmt.get("OWN_TS_NET_INCM_AMT") != null) {
				// 지배주주 당기순이익 합계를 이용하여 스코어를 계산한다.
				if(tsNetIncmAmt.get("OWN_TS_NET_INCM_AMT").compareTo(BigDecimal.ZERO) == 1) {
					result = "1";
				}else {
					result = "0";
				}
				
			// 지배주주 당기순이익 합계가 없고 당기순이익 합계가 있을 경우
			}else if(tsNetIncmAmt.get("TS_NET_INCM_AMT") != null) {
				// 당기순이익 합계를 이용하여 스코어를 계산한다.
				if(tsNetIncmAmt.get("TS_NET_INCM_AMT").compareTo(BigDecimal.ZERO) == 1) {
					result = "1";
				}else {
					result = "0";
				}
			}
		}
		
		return result;
	}
}
