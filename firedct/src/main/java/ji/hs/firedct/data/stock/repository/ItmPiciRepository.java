package ji.hs.firedct.data.stock.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ji.hs.firedct.data.stock.entity.ItmPici;
import ji.hs.firedct.data.stock.primary.ItmPiciPrimaryKey;

/**
 * 유상증자 Repository
 * @author now2woy
 *
 */
public interface ItmPiciRepository extends JpaRepository<ItmPici, ItmPiciPrimaryKey> {
	/**
	 * 
	 * @param itmCd
	 * @param dt
	 * @return
	 */
	@Query("SELECT COUNT(t) FROM ItmPici t WHERE t.itmCd = :itmCd AND t.noticeDt >= CONVERT(:dt, DATE)  - 365")
	public Long findCountByFscrFst(@Param("itmCd") String itmCd, @Param("dt") Date dt);
}
