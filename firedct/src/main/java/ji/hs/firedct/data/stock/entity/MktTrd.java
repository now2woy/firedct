package ji.hs.firedct.data.stock.entity;

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

import ji.hs.firedct.data.stock.primary.MktTrdPrimaryKey;
import lombok.Getter;
import lombok.Setter;

/**
 * 시장 거래 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "MKT_TRD")
@IdClass(MktTrdPrimaryKey.class)
public class MktTrd implements Serializable {
	private static final long serialVersionUID = 8761122646001616402L;
	
	/**
	 * 시장코드
	 */
	@Id
	@Column(name = "MKT_CD", nullable = false, length = 5)
	private String mktCd;
	
	/**
	 * 일자
	 */
	@Id
	@Column(name = "DT", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date dt;
	
	/**
	 * 종가
	 */
	@Column(name = "ED_AMT", nullable = true)
	private BigDecimal edAmt;
	
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
	 * VAL_01
	 */
	@Column(name = "VAL_01", nullable = true)
	private BigDecimal val01;
	
	/**
	 * VAL_02
	 */
	@Column(name = "VAL_02", nullable = true)
	private BigDecimal val02;
	
	/**
	 * VAL_03
	 */
	@Column(name = "VAL_03", nullable = true)
	private BigDecimal val03;
	
	/**
	 * VAL_04
	 */
	@Column(name = "VAL_04", nullable = true)
	private BigDecimal val04;
}
