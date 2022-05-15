package ji.hs.firedct.data.dart.primary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DART 공시 Entity Primary Key
 * @author now2woy
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DartNoticePrimaryKey implements Serializable {
	private static final long serialVersionUID = -6890701574199137923L;
	
	/**
	 * 종목 코드
	 */
	private String itmCd;
	
	/**
	 * 공시번호
	 */
	private String noticeNo;
}
