package ji.hs.firedct.itm.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 종목 재무제표 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString(exclude = {"itm"})
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "ITM_FINC_STS")
@IdClass(ItmFincStsPrimaryKey.class)
public class ItmFincSts implements Serializable {
	private static final long serialVersionUID = 1883193729454727018L;
	
	/**
	 * 종목 코드
	 */
	@Id
	@Column(name = "ITM_CD", nullable = false, length = 10)
	private String itmCd;
	
	/**
	 * 연도(년도)
	 */
	@Id
	@Column(name = "YR", nullable = false, length = 4)
	private String yr;
	
	/**
	 * 분기
	 */
	@Id
	@Column(name = "QT", nullable = false, length = 1)
	private String qt;
	
	/**
	 * 시장
	 */
	@Column(name = "MKT", nullable = false, length = 5)
	private String mkt;
	
	/**
	 * 자본
	 */
	@Column(name = "BSC_CPT", nullable = true, length = 20)
	private BigDecimal bscCpt;
	
	/**
	 * 지배 자본
	 */
	@Column(name = "OWN_CPT", nullable = true, length = 20)
	private BigDecimal ownCpt;
	
	/**
	 * 자산총계
	 */
	@Column(name = "AST_AMT", nullable = true, length = 20)
	private BigDecimal astAmt;
	
	/**
	 * 부채총계
	 */
	@Column(name = "DEBT_AMT", nullable = true, length = 20)
	private BigDecimal debtAmt;
	
	/**
	 * 매출액
	 */
	@Column(name = "SAL_AMT", nullable = true, length = 20)
	private BigDecimal salAmt;
	
	/**
	 * 합계 매출액
	 */
	@Column(name = "ADD_SAL_AMT", nullable = true, length = 20)
	private BigDecimal addSalAmt;
	
	/**
	 * 영업이익금액
	 */
	@Column(name = "OPR_INCM_AMT", nullable = true, length = 20)
	private BigDecimal oprIncmAmt;
	
	/**
	 * 합계 영업이익금액
	 */
	@Column(name = "ADD_OPR_INCM_AMT", nullable = true, length = 20)
	private BigDecimal addOprIncmAmt;
	
	/**
	 * 당기순이익금액
	 */
	@Column(name = "TS_NET_INCM_AMT", nullable = true, length = 20)
	private BigDecimal tsNetIncmAmt;
	
	/**
	 * 합계 당기순이익금액
	 */
	@Column(name = "ADD_TS_NET_INCM_AMT", nullable = true, length = 20)
	private BigDecimal addTsNetIncmAmt;
	
	/**
	 * 지배주주당기순이익금액
	 */
	@Column(name = "OWN_TS_NET_INCM_AMT", nullable = true, length = 20)
	private BigDecimal ownTsNetIncmAmt;
	
	/**
	 * 합계 지배주주당기순이익금액
	 */
	@Column(name = "ADD_OWN_TS_NET_INCM_AMT", nullable = true, length = 20)
	private BigDecimal addOwnTsNetIncmAmt;
	
	/**
	 * 당기기본주당순이익
	 */
	@Column(name = "TS_BSC_EPS", nullable = true)
	private BigDecimal tsBscEps;
	
	/**
	 * 합계 당기기본주당순이익
	 */
	@Column(name = "ADD_TS_BSC_EPS", nullable = true)
	private BigDecimal addTsBscEps;
	
	/**
	 * 당기희석주당순이익
	 */
	@Column(name = "TS_DLTD_EPS", nullable = true)
	private BigDecimal tsDltdEps;
	
	/**
	 * 합계 당기희석주당순이익
	 */
	@Column(name = "ADD_TS_DLTD_EPS", nullable = true)
	private BigDecimal addTsDltdEps;
	
	/**
	 * 계속영업기본주당순이익
	 */
	@Column(name = "OPR_BSC_EPS", nullable = true)
	private BigDecimal oprBscEps;
	
	/**
	 * 합계 계속영업기본주당순이익
	 */
	@Column(name = "ADD_OPR_BSC_EPS", nullable = true)
	private BigDecimal addOprBscEps;
	
	/**
	 * 계속영업희석주당순이익
	 */
	@Column(name = "OPR_DLTD_EPS", nullable = true)
	private BigDecimal oprDltdEps;
	
	/**
	 * 합계 계속영업희석주당순이익
	 */
	@Column(name = "ADD_OPR_DLTD_EPS", nullable = true)
	private BigDecimal addOprDltdEps;
	
	/**
	 * 영업활동현금흐름
	 */
	@Column(name = "OPR_CSFLW", nullable = true)
	private BigDecimal oprCsflw;
	
	/**
	 * ROE
	 */
	@Column(name = "ROE", nullable = true)
	private BigDecimal roe;
	
	/**
	 * ROA
	 */
	@Column(name = "ROA", nullable = true)
	private BigDecimal roa;
	
	/**
	 * 부채비율
	 */
	@Column(name = "DEBT_RT", nullable = true)
	private BigDecimal debtRt;
	
	/**
	 * 기준일자
	 */
	@Column(name = "STD_DT", nullable = true)
	@Temporal(TemporalType.DATE)
	private Date stdDt;
	
	@ManyToOne
	@JoinColumn(name = "ITM_CD", nullable = false, insertable = false, updatable = false)
	@JsonManagedReference
	private Itm itm;
	
	/**
	 * 당기순이익 합계
	 */
	@Transient
	private BigDecimal sumTsNetIncmAmt;
	
	/**
	 * 당기순이익 합계 건수
	 */
	@Transient
	private int sumTsNetIncmAmtCnt;
	
	/**
	 * 지배당기순이익 합계
	 */
	@Transient
	private BigDecimal sumOwnTsNetIncmAmt;
	
	/**
	 * 지배당기순이익 합계 건수
	 */
	@Transient
	private int sumOwnTsNetIncmAmtCnt;
	
	@Transient
	private String temp1;
	
	@Transient
	private String temp2;
}
