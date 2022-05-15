package ji.hs.firedct.data.stock.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 프로그램 실행 대상 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "PGR_EXEC_TG")
public class PgrExecTg implements Serializable {
	private static final long serialVersionUID = -6334201932835441537L;

	/**
	 * 프로그램코드
	 */
	@Id
	@Column(name = "PGR_CD", nullable = false)
	private String pgrCd;
	
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
}
