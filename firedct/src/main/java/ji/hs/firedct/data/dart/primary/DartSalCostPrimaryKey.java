package ji.hs.firedct.data.dart.primary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DART 임시 매출원가 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DartSalCostPrimaryKey implements Serializable {
	private static final long serialVersionUID = 126702254669411464L;
	
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
