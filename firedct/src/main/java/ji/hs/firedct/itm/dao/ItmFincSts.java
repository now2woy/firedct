package ji.hs.firedct.itm.dao;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

/**
 * 종목 재무제표 Entity
 * @author now2woy
 *
 */
@Data
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
	 * 시장
	 */
	@Id
	@Column(name = "MKT", nullable = false, length = 5)
	private String mkt;
	
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
	 * 매출액
	 */
	@Column(name = "SAL_AMT", nullable = true, length = 20)
	private BigDecimal salAmt;
	
	/**
	 * 영업이익금액
	 */
	@Column(name = "OPR_INCM_AMT", nullable = true, length = 20)
	private BigDecimal oprIncmAmt;
	
	/**
	 * 당기순이익금액
	 */
	@Column(name = "TS_NET_INCM_AMT", nullable = true, length = 20)
	private BigDecimal tsNetIncmAmt;
}
