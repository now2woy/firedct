package ji.hs.firedct.pgr.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 프로그램 잠금 Repository
 * @author now2woy
 *
 */
public interface PgrLokRepository extends JpaRepository<PgrLok, String> {
	/**
	 *  pgrCd에 해당하는 자료 조회
	 * @param pgrCd
	 * @return
	 */
	public Optional<PgrLok> findByPgrCd(String pgrCd);
}
