package ji.hs.firedct.batch.backtest.dao;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 백 테스트 거래 내역 VO
 * @author now2woy
 *
 */
@Data
public class BackTestTrdVO {
	/**
	 * 전략
	 */
	private String tactic;
	
	/**
	 * 종목코드
	 */
	private String itmCd;
	
	/**
	 * 종목명
	 */
	private String itmNm;
	
	/**
	 * 상장시장
	 */
	private String mkt;
	
	/**
	 * 매수일
	 */
	private String buyDt;
	
	/**
	 * 매수 단가
	 */
	private BigDecimal buyPrc;
	
	/**
	 * 매수 수량
	 */
	private BigDecimal buyQty;
	
	/**
	 * 매수 금액
	 */
	private BigDecimal buyAmt;
	
	/**
	 * 매도일
	 */
	private String sellDt;
	
	/**
	 * 매도 단가
	 */
	private BigDecimal sellPrc;
	
	/**
	 * 매도 수량
	 */
	private BigDecimal sellQty;
	
	/**
	 * 매도 금액
	 */
	private BigDecimal sellAmt;
	
	/**
	 * 수수료
	 */
	private BigDecimal fee;
	
	/**
	 * 세금
	 */
	private BigDecimal tax;
	
	/**
	 * 손익금액
	 */
	private BigDecimal incmAmt;
	
	/**
	 * 손익률
	 */
	private BigDecimal incmRt;
}
