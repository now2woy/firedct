package ji.hs.firedct.cd.dao;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "CD")
@IdClass(CdPrimaryKey.class)
public class Cd implements Serializable {
	private static final long serialVersionUID = 4833283522160960629L;

	@Id
	@Column(name = "CLS", nullable = false, length = 5)
	private String cls;
	
	@Column(name = "CLS_NM", nullable = true)
	private String clsNm;
	
	@Id
	@Column(name = "CD", nullable = false, length = 5)
	private String cd;
	
	@Column(name = "CD_NM", nullable = true)
	private String cdNm;
	
	@Column(name = "USE_YN", nullable = false, length = 1)
	@ColumnDefault("'Y'")
	private String useYn;
}
