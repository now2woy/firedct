package ji.hs.firedct.batch.calc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.ItmFincSts;

/**
 * ROE 계산기
 * @author now2woy
 *
 */
public class RoeCalc {
	/**
	 * ROE 계산
	 * @param itmFincStss
	 * @param ownCpt
	 * @param bscCpt
	 * @return
	 */
	public static BigDecimal calc(final List<ItmFincSts> itmFincStss, final BigDecimal ownCpt, final BigDecimal bscCpt) {
		BigDecimal result = null;
		
		var val = new HashMap<String, BigDecimal>();
		var cnt = new HashMap<String, Integer>();
		
		val.put("SUM_TS_NET_INCM_AMT", null);
		val.put("SUM_OWN_TS_NET_INCM_AMT", null);
		
		cnt.put("SUM_TS_NET_INCM_AMT_CNT", 0);
		cnt.put("SUM_OWN_TS_NET_INCM_AMT_CNT", 0);
		
		itmFincStss.stream().forEach(itmFincSts -> {
			// 당기순이익이 있을 경우
			val.put("SUM_TS_NET_INCM_AMT", Utils.add(val.get("SUM_TS_NET_INCM_AMT"), itmFincSts.getTsNetIncmAmt()));
			cnt.put("SUM_TS_NET_INCM_AMT_CNT", Utils.addCnt(cnt.get("SUM_TS_NET_INCM_AMT_CNT"), val.get("SUM_TS_NET_INCM_AMT"), itmFincSts.getTsNetIncmAmt()));
			
			// 지배당기순이익이 있을 경우
			val.put("SUM_OWN_TS_NET_INCM_AMT", Utils.add(val.get("SUM_OWN_TS_NET_INCM_AMT"), itmFincSts.getOwnTsNetIncmAmt()));
			cnt.put("SUM_OWN_TS_NET_INCM_AMT_CNT", Utils.addCnt(cnt.get("SUM_OWN_TS_NET_INCM_AMT_CNT"), val.get("SUM_OWN_TS_NET_INCM_AMT"), itmFincSts.getOwnTsNetIncmAmt()));
		});
		
		// 지배자본이 있을 경우
		if(ownCpt != null) {
			// 합계 지배당기순이익이 있고 합계 횟수가 4번일 경우
			if(val.get("SUM_OWN_TS_NET_INCM_AMT") != null && cnt.get("SUM_OWN_TS_NET_INCM_AMT_CNT") == 4) {
				/** ROE = (지배당기순이익 / 지배자본) * 100 */
				result = Utils.multiply(Utils.divide(val.get("SUM_OWN_TS_NET_INCM_AMT"), ownCpt, 10), new BigDecimal("100"), 2);
				
			// 합계 당기순이익이 있고 합계 횟수가 4번일 경우
			}else if(val.get("SUM_TS_NET_INCM_AMT") != null && cnt.get("SUM_TS_NET_INCM_AMT_CNT") == 4) {
				/** ROE = (당기순이익 / 지배자본) * 100 */
				result = Utils.multiply(Utils.divide(val.get("SUM_TS_NET_INCM_AMT"), ownCpt, 10), new BigDecimal("100"), 2);
			}
			
		// 자본이 있을 경우
		}else if(bscCpt != null) {
			// 합계 지배당기순이익이 있고 합계 횟수가 4번일 경우
			if(val.get("SUM_OWN_TS_NET_INCM_AMT") != null && cnt.get("SUM_OWN_TS_NET_INCM_AMT_CNT") == 4) {
				/** ROE = (지배당기순이익 / 자본) * 100 */
				result = Utils.multiply(Utils.divide(val.get("SUM_OWN_TS_NET_INCM_AMT"), bscCpt, 10), new BigDecimal("100"), 2);
				
			// 합계 당기순이익이 있고 합계 횟수가 4번일 경우
			}else if(val.get("SUM_TS_NET_INCM_AMT") != null && cnt.get("SUM_TS_NET_INCM_AMT_CNT") == 4) {
				/** ROE = (당기순이익 / 자본) * 100 */
				result = Utils.multiply(Utils.divide(val.get("SUM_TS_NET_INCM_AMT"), bscCpt, 10), new BigDecimal("100"), 2);
			}
		}
		
		return result;
	}
}
