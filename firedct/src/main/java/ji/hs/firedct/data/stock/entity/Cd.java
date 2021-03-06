package ji.hs.firedct.data.stock.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import ji.hs.firedct.data.stock.primary.CdPrimaryKey;
import lombok.Data;

/**
 * 코드 Entity
 * @author now2woy
 *
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "CD")
@IdClass(CdPrimaryKey.class)
public class Cd implements Serializable {
	private static final long serialVersionUID = 4833283522160960629L;
	
	/**
	 * 분류코드
	 */
	@Id
	@Column(name = "CLS", nullable = false, length = 5)
	private String cls;
	
	/**
	 * 분류코드명
	 */
	@Column(name = "CLS_NM", nullable = true)
	private String clsNm;
	
	/**
	 * 코드
	 */
	@Id
	@Column(name = "CD", nullable = false, length = 5)
	private String cd;
	
	/**
	 * 코드명
	 */
	@Column(name = "CD_NM", nullable = true)
	private String cdNm;
	
	/**
	 * 코드보조명
	 */
	@Column(name = "CD_SUB_NM", nullable = true)
	private String cdSubNm;
	
	/**
	 * 사용여부
	 */
	@Column(name = "USE_YN", nullable = false, length = 1)
	@ColumnDefault("'Y'")
	private String useYn;
}
