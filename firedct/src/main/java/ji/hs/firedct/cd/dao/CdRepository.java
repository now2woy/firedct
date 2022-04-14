package ji.hs.firedct.cd.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 코드 Repository
 * @author now2woy
 *
 */
public interface CdRepository extends JpaRepository<Cd, CdPrimaryKey> {
	/**
	 * 분류코드로 코드 목록 조회
	 * @param cls
	 * @return
	 */
	public List<Cd> findByCls(String cls);
	
	/**
	 * 분류코드와 코드로 코드 목록 조회
	 * @param cls
	 * @param cd
	 * @return
	 */
	public Cd findByClsAndCd(String cls, String cd);
	
	/**
	 * 
	 * @param cls
	 * @param cd
	 * @return
	 */
	@Query("SELECT c.cdNm FROM Cd c WHERE c.cls = :cls AND c.cd = :cd")
	public String findCdNmByClsAndCd(@Param("cls") String cls, @Param("cd") String cd);
	
	@Query("SELECT c.cd FROM Cd c WHERE c.cls = :cls AND c.cdSubNm = :cdSubNm")
	public String findCdByClsAndCdSubNm(@Param("cls") String cls, @Param("cdSubNm") String cdSubNm);
}
