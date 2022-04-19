package ji.hs.firedct.batch.tactic.dao;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 전략 VO
 * @author now2woy
 *
 */
@Data
public class TacticVO {
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
	 * 365일 최저 종가
	 */
	private BigDecimal minEdAmt;
	
	/**
	 * 종가
	 */
	private BigDecimal edAmt;
	
	/**
	 * 365일 최고 종가
	 */
	private BigDecimal maxEdAmt;
	
	/**
	 * 구매 목표가
	 */
	private BigDecimal tgEdAmt;
	
	/**
	 * 시가총액
	 */
	private BigDecimal mktTotAmt;
	
	/**
	 * PBR
	 */
	private BigDecimal pbr;
	
	/**
	 * PCR
	 */
	private BigDecimal pcr;
	
	/**
	 * PER
	 */
	private BigDecimal per;
	
	/**
	 * PSR
	 */
	private BigDecimal psr;
	
	/**
	 * 시가총액 순위
	 */
	private Long totAmtRank;
	
	/**
	 * PBR 순위
	 */
	private Long pbrRank;
	
	/**
	 * PCR 순위
	 */
	private Long pcrRank;
	
	/**
	 * PER 순위
	 */
	private Long perRank;
	
	/**
	 * PSR 순위
	 */
	private Long psrRank;
	
	/**
	 * 합계 순위
	 */
	private Long totRank;
	
	/**
	 * 일자
	 */
	private String dt;
}