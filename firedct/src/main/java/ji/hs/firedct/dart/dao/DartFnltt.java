package ji.hs.firedct.dart.dao;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DART 임시 재무제표 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "DART_FNLTT")
@IdClass(DartFnlttPrimaryKey.class)
public class DartFnltt implements Serializable {
	private static final long serialVersionUID = -1436315584358827805L;
	
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
	 * 순번
	 */
	@Id
	@Column(name = "SEQ", nullable = false)
	private Long seq;
	
	/**
	 * 시장
	 */
	@Column(name = "MKT", nullable = false, length = 5)
	private String mkt;
	
	/**
	 * 분기코드
	 */
	@Column(name = "REPRT_CD", nullable = true)
	private String reprtCd;
	
	/**
	 * 구분코드
	 */
	@Column(name = "SJ_DIV", nullable = true)
	private String sjDiv;
	
	/**
	 * 구분명
	 */
	@Column(name = "SJ_NM", nullable = true)
	private String sjNm;
	
	/**
	 * 계정ID
	 */
	@Column(name = "ACNT_ID", nullable = true)
	private String acntId;
	
	/**
	 * 계정명
	 */
	@Column(name = "ACNT_NM", nullable = true)
	private String acntNm;
	
	/**
	 * 계정상세
	 */
	@Column(name = "ACNT_DTL", nullable = true)
	private String acntDtl;
	
	/**
	 * 이번분기명
	 */
	@Column(name = "TH_TM_NM", nullable = true)
	private String thTmNm;
	
	/**
	 * 이번분기값
	 */
	@Column(name = "TH_TM_AMT", nullable = true)
	private String thTmAmt;
	
	/**
	 * 이번분기누적값
	 */
	@Column(name = "TH_TM_ADD_AMT", nullable = true)
	private String thTmAddAmt;
	
	/**
	 * 이전분기명
	 */
	@Column(name = "FRM_TM_NM", nullable = true)
	private String frmTmNm;
	
	/**
	 * 이전분기값
	 */
	@Column(name = "FRM_TM_AMT", nullable = true)
	private String frmTmAmt;
	
	/**
	 * 이전분기누적값
	 */
	@Column(name = "FRM_TM_ADD_AMT", nullable = true)
	private String frmTmAddAmt;
	
	/**
	 * 순번
	 */
	@Column(name = "ORD", nullable = true)
	private String ord;
	
}
