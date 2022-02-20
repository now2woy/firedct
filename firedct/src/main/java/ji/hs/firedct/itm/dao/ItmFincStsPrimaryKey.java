package ji.hs.firedct.itm.dao;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 종목 재무제표 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItmFincStsPrimaryKey implements Serializable {
	private static final long serialVersionUID = -3822100306824452765L;
	
	/**
	 * 종목 코드
	 */
	private String itmCd;
	
	/**
	 * 시장
	 */
	private String mkt;
	
	/**
	 * 연도(년도)
	 */
	private String yr;
	
	/**
	 * 분기
	 */
	private String qt;
}
