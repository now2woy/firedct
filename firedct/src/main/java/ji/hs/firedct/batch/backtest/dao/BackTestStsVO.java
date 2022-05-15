package ji.hs.firedct.batch.backtest.dao;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 백 테스트 결산 내역 VO
 * @author now2woy
 *
 */
@Data
public class BackTestStsVO {
	/**
	 * 전략
	 */
	private String tactic;
	
	/**
	 * 순번
	 */
	private int seq;
	
	/**
	 * 매수일
	 */
	private String buyDt;

	/**
	 * 매도일
	 */
	private String sellDt;
	
	/**
	 * 입금액
	 */
	private BigDecimal dpsAmt;
	
	/**
	 * 이월금액
	 */
	private BigDecimal fwdAmt;
	
	/**
	 * 종목수
	 */
	private BigDecimal itmQty;
	
	/**
	 * 종목당할당금액
	 */
	private BigDecimal itmAsignAmt;
	
	/**
	 * 매수금액
	 */
	private BigDecimal buyAmt;
	
	/**
	 * 매수 잔액
	 */
	private BigDecimal buyBlncAmt;
	
	/**
	 * 매도 금액
	 */
	private BigDecimal sellAmt;
	
	/**
	 * 매도 잔액
	 */
	private BigDecimal sellBlncAmt;
	
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
