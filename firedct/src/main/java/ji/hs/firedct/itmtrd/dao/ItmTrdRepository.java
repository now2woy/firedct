package ji.hs.firedct.itmtrd.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItmTrdRepository extends JpaRepository<ItmTrd, ItmTrdPrimaryKey> {
	@Query("SELECT MAX(i.dt) AS dt FROM ItmTrd i where i.itmCd = :itmCd")
	public Date findMaxDtByItmCd(@Param("itmCd") String itmCd);
}
