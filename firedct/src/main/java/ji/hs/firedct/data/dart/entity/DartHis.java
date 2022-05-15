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
 * DART 수집 이력 Entity
 * @author now2woy
 *
 */
@Getter
@Setter
@ToString
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "DART_HIS")
public class DartHis implements Serializable {
	private static final long serialVersionUID = -5603544793826961876L;
	
	/**
	 * 종목코드
	 */
	@Id
	@Column(name = "ITM_CD", nullable = false, length = 10)
	private String itmCd;
}
