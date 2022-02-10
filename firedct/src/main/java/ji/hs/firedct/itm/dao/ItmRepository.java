package ji.hs.firedct.itm.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 종목 Repository
 * @author now2woy
 *
 */
public interface ItmRepository extends JpaRepository<Itm, ItmPrimaryKey> {
	/**
	 * itmCd에 해당하는 자료 조회
	 * @param itmCd
	 * @return
	 */
	public Optional<Itm> findByItmCd(String itmCd);
	
	/**
	 * Dart 종목 코드가 있는 자료 조회
	 * @param page
	 * @return
	 */
	public List<Itm> findByDartItmCdIsNotNull(Pageable page);
}
