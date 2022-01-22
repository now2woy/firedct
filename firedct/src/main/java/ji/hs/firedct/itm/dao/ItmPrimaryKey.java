package ji.hs.firedct.itm.dao;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItmPrimaryKey implements Serializable {
	private static final long serialVersionUID = -8483163181226471044L;
	
	private String itmCd;
	private String mkt;
}
