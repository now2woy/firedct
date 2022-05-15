package ji.hs.firedct.data.stock.entity;

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

import ji.hs.firedct.data.stock.primary.ItmPiciPrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 유상증자 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "ITM_PICI")
@IdClass(ItmPiciPrimaryKey.class)
public class ItmPici implements Serializable {
	private static final long serialVersionUID = 6186527977655447576L;
	
	/**
	 * 종목코드
	 */
	@Id
	@Column(name = "ITM_CD", nullable = false, length = 10)
	private String itmCd;
	
	/**
	 * 공시번호
	 */
	@Id
	@Column(name = "NOTICE_NO", nullable = false)
	private String noticeNo;
	
	/**
	 * 공시구분
	 */
	@Column(name = "NOTICE_CLS", nullable = true)
	private String noticeCls;
	
	/**
	 * 제목
	 */
	@Column(name = "TITLE", nullable = true)
	private String title;
	
	/**
	 * 공시일자
	 */
	@Column(name = "NOTICE_DT", nullable = true)
	@Temporal(TemporalType.DATE)
	private Date noticeDt;
}
