package ji.hs.firedct.data.dart.primary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DART 임시 재무제표 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DartFnlttPrimaryKey implements Serializable {
	private static final long serialVersionUID = 1497439590291409459L;
	
	/**
	 * 종목 코드
	 */
	private String itmCd;
	
	/**
	 * 연도(년도)
	 */
	private String yr;
	
	/**
	 * 분기
	 */
	private String qt;
	
	/**
	 * 순번
	 */
	private Long seq;
}
