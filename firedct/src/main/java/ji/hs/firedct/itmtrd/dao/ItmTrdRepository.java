package ji.hs.firedct.itmtrd.dao;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItmTrdRepository extends JpaRepository<ItmTrd, ItmTrdPrimaryKey> {
	public Page<ItmTrd> findByTmpDtIsNull(Pageable pageable);
	
	@Query("SELECT MAX(i.tmpDt) AS tmpDt FROM ItmTrd i where i.itmCd = :itmCd")
	public Date findMaxTmpDtByItmCd(@Param("itmCd") String itmCd);
}
