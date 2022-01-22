package ji.hs.firedct.itmtrd.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItmTrdRepository extends JpaRepository<ItmTrd, ItmTrdPrimaryKey> {
	public Page<ItmTrd> findByTmpDtIsNull(Pageable pageable);
}
