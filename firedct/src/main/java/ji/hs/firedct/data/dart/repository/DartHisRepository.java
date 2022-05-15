package ji.hs.firedct.data.dart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ji.hs.firedct.data.dart.entity.DartHis;

/**
 * DART 수집 이력 Repository
 * @author now2woy
 *
 */
public interface DartHisRepository extends JpaRepository<DartHis, String> {
	/**
	 * 종목코드에 해당하는 이력이 있는지 조회
	 * @param itmCd
	 * @return
	 */
	public Optional<DartHis> findByItmCd(String itmCd);
}
