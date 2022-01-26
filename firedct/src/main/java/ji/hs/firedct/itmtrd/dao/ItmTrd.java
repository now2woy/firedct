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

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "ITM_TRD")
@IdClass(ItmTrdPrimaryKey.class)
public class ItmTrd implements Serializable {
	private static final long serialVersionUID = -3322597309763710368L;

	@Id
	@Column(name = "ITM_CD", nullable = false, length = 10)
	private String itmCd;
	
	@Id
	@Column(name = "DT", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date dt;
	
	@Column(name = "ED_AMT", nullable = false, length = 20)
	private BigDecimal edAmt;
	
	@Column(name = "INCR", nullable = false, length = 20)
	private BigDecimal incr;
	
	@Column(name = "TRD_QTY", nullable = false, length = 20)
	private BigDecimal trdQty;
	
	@Column(name = "TRD_AMT", nullable = false, length = 20)
	private BigDecimal trdAmt;
	
	@Column(name = "MKT_TOT_AMT", nullable = false, length = 20)
	private BigDecimal mktTotAmt;
	
	@Column(name = "ISU_STK_QTY", nullable = false, length = 20)
	private BigDecimal isuStkQty;
}
