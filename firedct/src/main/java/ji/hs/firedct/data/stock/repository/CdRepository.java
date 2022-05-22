package ji.hs.firedct.data.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ji.hs.firedct.data.stock.entity.Cd;
import ji.hs.firedct.data.stock.primary.CdPrimaryKey;

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
	 * 코드로 코드명 조회
	 * @param cls
	 * @param cd
	 * @return
	 */
	@Query("SELECT c.cdNm FROM Cd c WHERE c.cls = :cls AND c.cd = :cd")
	public String findCdNmByClsAndCd(@Param("cls") String cls, @Param("cd") String cd);
	
	/**
	 * 코드명으로 코드 조회
	 * @param cls
	 * @param cdNm
	 * @return
	 */
	@Query("SELECT c.cd FROM Cd c WHERE c.cls = :cls AND c.cdNm = :cdNm")
	public String findCdByClsAndCdNm(@Param("cls") String cls, @Param("cdNm") String cdNm);
	
	/**
	 * 코드보조명으로 코드 조회
	 * @param cls
	 * @param cdSubNm
	 * @return
	 */
	@Query("SELECT c.cd FROM Cd c WHERE c.cls = :cls AND c.cdSubNm = :cdSubNm")
	public String findCdByClsAndCdSubNm(@Param("cls") String cls, @Param("cdSubNm") String cdSubNm);
}
