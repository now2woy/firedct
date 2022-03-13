package ji.hs.firedct.pgr.dao;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프로그램 실행 대상 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PgrExecTgPrimaryKey implements Serializable {
	private static final long serialVersionUID = 1742887946090640201L;

	/**
	 * 프로그램코드
	 */
	private String pgrCd;
	
	/**
	 * 순번
	 */
	private Long seq;
}
