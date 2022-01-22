package ji.hs.firedct.cd.dao;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CdPrimaryKey implements Serializable {
	private static final long serialVersionUID = 4515765868134611019L;
	
	private String cls;
	private String cd;
}