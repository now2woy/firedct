package ji.hs.firedct.data.stock.primary;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 시장 거래 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MktTrdPrimaryKey implements Serializable {
	private static final long serialVersionUID = -3056693549769354667L;
	
	/**
	 * 시장코드
	 */
	private String mktCd;
	
	/**
	 * 일자
	 */
	private Date dt;
}
