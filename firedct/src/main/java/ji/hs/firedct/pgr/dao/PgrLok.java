package ji.hs.firedct.pgr.dao;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

/**
 * 프로그램 잠금 Entity
 * @author now2woy
 *
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "PGR_LOK")
public class PgrLok implements Serializable {
	private static final long serialVersionUID = 2039012478514339131L;

	/**
	 * 프로그램코드
	 */
	@Id
	@Column(name = "PGR_CD", length = 5)
	private String pgrCd;
	
	/**
	 * 잠금중여부
	 */
	@Column(name = "LOK_ING_YN", length = 1)
	private String lokIngYn;
}
