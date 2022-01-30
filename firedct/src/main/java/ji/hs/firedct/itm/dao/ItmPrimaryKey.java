package ji.hs.firedct.itm.dao;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 종목 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItmPrimaryKey implements Serializable {
	private static final long serialVersionUID = -8483163181226471044L;
	
	/**
	 * 종목코드
	 */
	private String itmCd;
	
	/**
	 * 상장시장
	 */
	private String mkt;
}
