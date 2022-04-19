package ji.hs.firedct.data.stock.cd.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 코드 Entity Primary Key
 * @author now2w
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CdPrimaryKey implements Serializable {
	private static final long serialVersionUID = 4515765868134611019L;
	/**
	 * 분류코드
	 */
	private String cls;
	
	/**
	 * 코드
	 */
	private String cd;
}
