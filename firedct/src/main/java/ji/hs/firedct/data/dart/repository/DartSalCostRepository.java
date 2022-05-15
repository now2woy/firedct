package ji.hs.firedct.data.dart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ji.hs.firedct.data.dart.entity.DartSalCost;
import ji.hs.firedct.data.dart.primary.DartSalCostPrimaryKey;

/**
 * DART 임시 매출원가 Repository
 * @author now2woy
 *
 */
public interface DartSalCostRepository extends JpaRepository<DartSalCost, DartSalCostPrimaryKey> {

}
