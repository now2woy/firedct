package ji.hs.firedct.itm.dao;

import java.io.Serializable;
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
@Table(name = "ITM")
@IdClass(ItmPrimaryKey.class)
public class Itm implements Serializable {
	private static final long serialVersionUID = -2553168850727723633L;

	@Id
	@Column(name = "ITM_CD", nullable = false, length = 10)
	private String itmCd;
	
	@Column(name = "ITM_NM", nullable = false)
	private String itmNm;
	
	@Id
	@Column(name = "MKT", nullable = false, length = 5)
	private String mkt;
	
	@Column(name = "STD_ITM_CD", nullable = false, length = 20)
	private String stdItmCd;
	
	@Column(name = "PUB_DT", nullable = true)
	@Temporal(TemporalType.DATE)
	private Date pubDt;
}
