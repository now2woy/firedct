package ji.hs.firedct.data.dart.entity;

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

import ji.hs.firedct.data.dart.primary.DartNoticePrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DART 공시 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "DART_NOTICE")
@IdClass(DartNoticePrimaryKey.class)
public class DartNotice implements Serializable {
	private static final long serialVersionUID = 2695753793018772979L;
	
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
