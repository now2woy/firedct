package ji.hs.firedct.cd.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
