package ji.hs.firedct.itmtrd.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

/**
 * 종목 거래 Entity
 * @author now2woy
 *
 */
@Data
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
}
