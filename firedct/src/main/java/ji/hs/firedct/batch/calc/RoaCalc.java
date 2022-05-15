package ji.hs.firedct.batch.calc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.ItmFincSts;

/**
 * ROA 계산기
 * @author now2woy
 *
 */
public class RoaCalc {
	/**
	 * ROA 계산
	 * @param itmFincStss
	 * @param astAmt
	 * @return
	 */
	public static BigDecimal calc(final List<ItmFincSts> itmFincStss, final BigDecimal astAmt) {
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
		
		// 자산총계가 있을 경우
		if(astAmt != null) {
			// 합계 지배당기순이익이 있고 합계 횟수가 4번일 경우
			if(val.get("SUM_OWN_TS_NET_INCM_AMT") != null && cnt.get("SUM_OWN_TS_NET_INCM_AMT_CNT") == 4) {
				/** ROA = (지배당기순이익 / 자산총계) * 100 */
				result = Utils.multiply(Utils.divide(val.get("SUM_OWN_TS_NET_INCM_AMT"), astAmt, 10), new BigDecimal("100"), 2);
				
			// 합계 당기순이익이 있고 합계 횟수가 4번일 경우
			}else if(val.get("SUM_TS_NET_INCM_AMT") != null && cnt.get("SUM_TS_NET_INCM_AMT_CNT") == 4) {
				/** ROA = (당기순이익 / 자산총계) * 100 */
				result = Utils.multiply(Utils.divide(val.get("SUM_TS_NET_INCM_AMT"), astAmt, 10), new BigDecimal("100"), 2);
			}
		}
		
		return result;
	}
}
