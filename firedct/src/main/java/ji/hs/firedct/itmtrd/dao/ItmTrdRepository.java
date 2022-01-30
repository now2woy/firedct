package ji.hs.firedct.itmtrd.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 종목 거래 Repository
 * @author now2woy
 *
 */
public interface ItmTrdRepository extends JpaRepository<ItmTrd, ItmTrdPrimaryKey> {
	/**
	 * 종목코드에 해당하는 최종 거래일자 조회
	 * @param itmCd
	 * @return
	 */
	@Query("SELECT MAX(i.dt) AS dt FROM ItmTrd i where i.itmCd = :itmCd")
	public Date findMaxDtByItmCd(@Param("itmCd") String itmCd);
}
