package ji.hs.firedct.data.stock.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ji.hs.firedct.data.stock.entity.PgrExecTg;

/**
 * 프로그램 실행 대상 Repository
 * @author now2woy
 *
 */
public interface PgrExecTgRepository extends JpaRepository<PgrExecTg, String> {
	/**
	 * 
	 * @param pgrCd
	 * @param page
	 * @return
	 */
	public PgrExecTg findByPgrCd(String pgrCd);
}
