package ji.hs.firedct.data.dart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ji.hs.firedct.data.dart.entity.DartKey;

/**
 * DART API Key Repository
 * @author now2woy
 *
 */
public interface DartKeyRepository extends JpaRepository<DartKey, String> {

}
