package ji.hs.firedct.data.dart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ji.hs.firedct.data.dart.entity.DartFnltt;
import ji.hs.firedct.data.dart.primary.DartFnlttPrimaryKey;

/**
 * DART 임시 재무제표 Repository
 * @author now2woy
 *
 */
public interface DartFnlttRepository extends JpaRepository<DartFnltt, DartFnlttPrimaryKey> {
	/**
	 * DART 임시 재무제표 조회
	 * @param itmCd
	 * @param yr
	 * @param qt
	 * @return
	 */
	public List<DartFnltt> findByItmCdAndYrAndQtOrderBySeqAsc(String itmCd, String yr, String qt);
}
