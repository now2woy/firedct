package ji.hs.firedct.itmtrd.dao;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItmTrdPrimaryKey implements Serializable {
	private static final long serialVersionUID = -2915396440952031417L;
	
	private String itmCd;
	private String dt;
}
