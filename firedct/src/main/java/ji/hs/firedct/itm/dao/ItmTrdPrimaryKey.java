package ji.hs.firedct.itm.dao;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 종목 거래 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItmTrdPrimaryKey implements Serializable {
	private static final long serialVersionUID = -2915396440952031417L;
	
	/**
	 * 종목 코드
	 */
	private String itmCd;
	
	/**
	 * 거래일자
	 */
	private Date dt;
}
