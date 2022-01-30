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

/**
 * 종목 Entity
 * @author now2woy
 *
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "ITM")
@IdClass(ItmPrimaryKey.class)
public class Itm implements Serializable {
	private static final long serialVersionUID = -2553168850727723633L;
	
	/**
	 * 종목코드
	 */
	@Id
	@Column(name = "ITM_CD", length = 10)
	private String itmCd;
	
	/**
	 * 종목명
	 */
	@Column(name = "ITM_NM")
	private String itmNm;
	
	/**
	 * 상장시장
	 */
	@Id
	@Column(name = "MKT", length = 5)
	private String mkt;
	
	/**
	 * KRX 종목코드
	 */
	@Column(name = "STD_ITM_CD", length = 20)
	private String stdItmCd;
	
	/**
	 * Dart 종목코드
	 */
	@Column(name = "DART_ITM_CD", length = 20)
	private String dartItmCd;
	
	/**
	 * 상장일자
	 */
	@Column(name = "PUB_DT")
	@Temporal(TemporalType.DATE)
	private Date pubDt;
}
