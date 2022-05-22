package ji.hs.firedct.batch.calc;

import java.math.BigDecimal;

import ji.hs.firedct.co.Utils;

/**
 *부채비율 계산기
 * @author now2woy
 *
 */
public class DebtRtCalc {
	/**
	 * 부채비율 계산
	 * @param bscCpt
	 * @param ownCpt
	 * @param debtAmt
	 * @return
	 */
	public static BigDecimal calc(BigDecimal bscCpt, BigDecimal ownCpt, BigDecimal debtAmt) {
		BigDecimal result = null;
		// 자본 또는 지배자본과 부채가 있을 경우
		if((bscCpt != null || ownCpt != null) && debtAmt != null) {
			// 부채가 0이 아닐 경우
			if(debtAmt.compareTo(BigDecimal.ZERO) != 0) {
				// 자본이 0이 아닐 경우
				if(bscCpt != null && bscCpt.compareTo(BigDecimal.ZERO) != 0) {
					/** 부채비율 = 부채총계 / 자본 * 100 */
					result = Utils.multiply(Utils.divide(debtAmt, bscCpt, 10), new BigDecimal("100"), 2);
					
				// 지배자본이 0이 아닐 경우
				} else if(ownCpt != null && ownCpt.compareTo(BigDecimal.ZERO) != 0) {
					/** 부채비율 = 부채총계 / 지배자본 * 100 */
					result = Utils.multiply(Utils.divide(debtAmt, ownCpt, 10), new BigDecimal("100"), 2);
				}
			}
		}
		
		return result;
	}
}
