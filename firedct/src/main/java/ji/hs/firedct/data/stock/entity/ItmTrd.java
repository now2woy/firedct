package ji.hs.firedct.data.stock.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import ji.hs.firedct.data.stock.primary.ItmTrdPrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 종목 거래 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString(exclude = {"itm"})
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "ITM_TRD")
@IdClass(ItmTrdPrimaryKey.class)
public class ItmTrd implements Serializable {
	private static final long serialVersionUID = -3322597309763710368L;
	
	/**
	 * 종목코드
	 */
	@Id
	@Column(name = "ITM_CD", nullable = false, length = 10)
	private String itmCd;
	
	/**
	 * 거래일자
	 */
	@Id
	@Column(name = "DT", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date dt;
	
	/**
	 * 종가
	 */
	@Column(name = "ED_AMT", nullable = false, length = 20)
	private BigDecimal edAmt;
	
	/**
	 * 전일대비증감금액
	 */
	@Column(name = "INCR", nullable = false, length = 20)
	private BigDecimal incr;
	
	/**
	 * 거래량
	 */
	@Column(name = "TRD_QTY", nullable = false, length = 20)
	private BigDecimal trdQty;
	
	/**
	 * 거래금액
	 */
	@Column(name = "TRD_AMT", nullable = false, length = 20)
	private BigDecimal trdAmt;
	
	/**
	 * 시총금액
	 */
	@Column(name = "MKT_TOT_AMT", nullable = false, length = 20)
	private BigDecimal mktTotAmt;
	
	/**
	 * 발행주식수
	 */
	@Column(name = "ISU_STK_QTY", nullable = false, length = 20)
	private BigDecimal isuStkQty;
	
	/**
	 * 초단기이동평균금액(5일이동평균금액)
	 */
	@Column(name = "VSTTM_MV_AVG_AMT", nullable = true, length = 20)
	private BigDecimal vsttmMvAvgAmt;
	
	/**
	 * 단기이동평균금액(20일이동평균금액)
	 */
	@Column(name = "STTM_MV_AVG_AMT", nullable = true, length = 20)
	private BigDecimal sttmMvAvgAmt;
	
	/**
	 * 중기이동평균금액(60일이동평균금액)
	 */
	@Column(name = "MDTM_MV_AVG_AMT", nullable = true, length = 20)
	private BigDecimal mdtmMvAvgAmt;
	
	/**
	 * 장기이동평균금액(120일이동평균금액)
	 */
	@Column(name = "LNTM_MV_AVG_AMT", nullable = true, length = 20)
	private BigDecimal lntmMvAvgAmt;
	
	/**
	 * 거래회전율(당일거래주식수 / 전체주식수)
	 */
	@Column(name = "TRD_TNOV_RT", nullable = true)
	private BigDecimal trdTnovRt;
	
	/**
	 * PER
	 */
	@Column(name = "PER", nullable = true)
	private BigDecimal per;
	
	/**
	 * PBR
	 */
	@Column(name = "PBR", nullable = true)
	private BigDecimal pbr;
	
	/**
	 * PSR
	 */
	@Column(name = "PCR", nullable = true)
	private BigDecimal pcr;
	
	/**
	 * PSR
	 */
	@Column(name = "PSR", nullable = true)
	private BigDecimal psr;
	
	/**
	 * BPS
	 */
	@Column(name = "BPS", nullable = true)
	private BigDecimal bps;
	
	/**
	 * CPS
	 */
	@Column(name = "CPS", nullable = true)
	private BigDecimal cps;
	
	/**
	 * SPS
	 */
	@Column(name = "SPS", nullable = true)
	private BigDecimal sps;
	
	@ManyToOne
	@JoinColumn(name = "ITM_CD", nullable = false, insertable = false, updatable = false)
	@JsonManagedReference
	private Itm itm;
	
	@Transient
	private List<ItmFincSts> itmFincStss;
	
	/**
	 * 합계 EPS(4분기 자료를 합해서 사용해야 한다.)
	 */
	@Transient
	private BigDecimal sumEps;
	
	/**
	 * 합계 EPS 건수
	 */
	@Transient
	private int sumEpsCnt;
	
	@Transient
	private BigDecimal totIsuStkQty;
	
	/**
	 * 합계 매출액
	 */
	@Transient
	private BigDecimal sumSalAmt;
	
	/**
	 * 합계 매출액 건수
	 */
	@Transient
	private int sumSalAmtCnt;
	
	/**
	 * 합계 영업활동현금흐름
	 */
	@Transient
	private BigDecimal sumOprCsflw;
	
	/**
	 * 합계 영업활동현금흐름 건수
	 */
	@Transient
	private int sumOprCsflwCnt;
	
	/**
	 * 종목명
	 */
	@Transient
	private String itmNm;
	
	/**
	 * 365일 최저 PER
	 */
	@Transient
	private BigDecimal minPer;
	
	/**
	 * 365일 최대 PER
	 */
	@Transient
	private BigDecimal maxPer;
	
	/**
	 * 365일 최저 PBR
	 */
	@Transient
	private BigDecimal minPbr;
	
	/**
	 * 365일 최대 PBR
	 */
	@Transient
	private BigDecimal maxPbr;
	
	/**
	 * ROE
	 */
	@Transient
	private BigDecimal roe;
	
	/**
	 * ROA
	 */
	@Transient
	private BigDecimal roa;
	
	/**
	 * 부채비율
	 */
	@Transient
	private BigDecimal debtRt;
	
	/**
	 * 일자 String
	 */
	@Transient
	private String dtStr;
	
	/**
	 * 356 최저 종가
	 */
	@Transient
	private BigDecimal minEdAmt;
	
	/**
	 * 365 최고 종가
	 */
	@Transient
	private BigDecimal maxEdAmt;
	
	/**
	 * 목표가
	 */
	@Transient
	private BigDecimal tgEdAmt;
}