package ji.hs.firedct.batch.tactic.dao;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 전략 VO
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
	 * ROE
	 */
	private BigDecimal roe;
	
	/**
	 * ROA
	 */
	private BigDecimal roa;
	
	/**
	 * 부채비율
	 */
	private BigDecimal debtRt;
	
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
	
	/**
	 * 순번
	 */
	private int seq;
	
	/**
	 * F-SCORE 1
	 */
	private String fscrFst;
	
	/**
	 * F-SCORE 2
	 */
	private String fscrSnd;
	
	/**
	 * F-SCORE 3
	 */
	private String fscrTrd;
	
	/**
	 * 값 01
	 */
	private BigDecimal val01;
	
	/**
	 * 값 02
	 */
	private BigDecimal val02;
	
	/**
	 * 값 03
	 */
	private BigDecimal val03;
	
	/**
	 * 값 04
	 */
	private BigDecimal val04;
	
	/**
	 * 값 05
	 */
	private BigDecimal val05;
	
	/**
	 * 값 06
	 */
	private BigDecimal val06;
	
	/**
	 * 값 07
	 */
	private BigDecimal val07;
	
	/**
	 * 값 08
	 */
	private BigDecimal val08;
	
	/**
	 * 값 09
	 */
	private BigDecimal val09;
	
	/**
	 * 값 10
	 */
	private BigDecimal val10;
	
	/**
	 * 값 11
	 */
	private BigDecimal val11;
	
	/**
	 * 값 12
	 */
	private BigDecimal val12;
	
	/**
	 * 값 13
	 */
	private BigDecimal val13;
	
	/**
	 * 값 14
	 */
	private BigDecimal val14;
	
	/**
	 * 값 15
	 */
	private BigDecimal val15;
	
	/**
	 * 값 16
	 */
	private BigDecimal val16;
	
	/**
	 * 값 17
	 */
	private BigDecimal val17;
	
	/**
	 * 값 18
	 */
	private BigDecimal val18;
	
	/**
	 * 값 19
	 */
	private BigDecimal val19;
	
	/**
	 * 값 20
	 */
	private BigDecimal val20;
	
	/**
	 * 값 21
	 */
	private BigDecimal val21;
	
	/**
	 * 값 22
	 */
	private BigDecimal val22;
	
	/**
	 * 값 23
	 */
	private BigDecimal val23;
	
	/**
	 * 값 24
	 */
	private BigDecimal val24;
	
	/**
	 * 값 25
	 */
	private BigDecimal val25;
	
	/**
	 * 값 26
	 */
	private BigDecimal val26;
	
	/**
	 * 값 27
	 */
	private BigDecimal val27;
	
	/**
	 * 값 28
	 */
	private BigDecimal val28;
	
	/**
	 * 값 29
	 */
	private BigDecimal val29;
	
	/**
	 * 값 30
	 */
	private BigDecimal val30;
}