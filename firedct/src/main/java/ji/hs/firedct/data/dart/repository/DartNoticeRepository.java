package ji.hs.firedct.data.dart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ji.hs.firedct.data.dart.entity.DartNotice;
import ji.hs.firedct.data.dart.primary.DartNoticePrimaryKey;

/**
 * DART 공시 Repository
 * @author now2woy
 *
 */
public interface DartNoticeRepository extends JpaRepository<DartNotice, DartNoticePrimaryKey> {

}
