package ji.hs.firedct.itm.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 종목 Repository
 * @author now2woy
 *
 */
public interface ItmRepository extends JpaRepository<Itm, ItmPrimaryKey> {
	public Optional<Itm> findByItmCd(String itmCd);
}
