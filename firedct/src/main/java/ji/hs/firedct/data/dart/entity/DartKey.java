package ji.hs.firedct.data.dart.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DART API KEY Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "DART_KEY")
public class DartKey implements Serializable {
	private static final long serialVersionUID = -4419403721685619050L;
	
	/**
	 * DART API KEY
	 */
	@Id
	@Column(name = "DART_API_KEY", nullable = false)
	private String dartApiKey;
}
