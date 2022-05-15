package ji.hs.firedct.data.stock.primary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 유상증자 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItmPiciPrimaryKey implements Serializable {
	private static final long serialVersionUID = 5212229526950467493L;
	
	/**
	 * 종목 코드
	 */
	private String itmCd;
	
	/**
	 * 공시번호
	 */
	private String noticeNo;
}
