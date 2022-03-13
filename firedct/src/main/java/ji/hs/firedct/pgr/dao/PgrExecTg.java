package ji.hs.firedct.pgr.dao;

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
 * 프로그램 실행 대상 Entity
 * @author now2woy
 *
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "PGR_EXEC_TG")
@IdClass(PgrExecTgPrimaryKey.class)
public class PgrExecTg implements Serializable {
	private static final long serialVersionUID = 1949509754444071014L;

	/**
	 * 프로그램코드
	 */
	@Id
	@Column(name = "PGR_CD", nullable = false, length = 5)
	private String pgrCd;
	
	/**
	 * 순번
	 */
	@Id
	@Column(name = "SEQ", nullable = false)
	private Long seq;
	
	/**
	 * 변수 1
	 */
	@Column(name = "PARAM_01", nullable = true)
	private String param01;
	/**
	 * 변수 2
	 */
	@Column(name = "PARAM_02", nullable = true)
	private String param02;
	/**
	 * 변수 3
	 */
	@Column(name = "PARAM_03", nullable = true)
	private String param03;
	/**
	 * 변수 4
	 */
	@Column(name = "PARAM_04", nullable = true)
	private String param04;
	/**
	 * 변수 5
	 */
	@Column(name = "PARAM_05", nullable = true)
	private String param05;
	
	/**
	 * 실행여부
	 */
	@Column(name = "EXEC_YN", nullable = false, length=1)
	private String execYn;
	
	/**
	 * 실행일시
	 */
	@Column(name = "EXEC_DT", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date execDt;
}
