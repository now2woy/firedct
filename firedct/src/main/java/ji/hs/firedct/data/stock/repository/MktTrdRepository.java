package ji.hs.firedct.data.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import ji.hs.firedct.data.stock.entity.MktTrd;
import ji.hs.firedct.data.stock.primary.MktTrdPrimaryKey;

/**
 * 시장 거래 Repository
 * @author now2woy
 *
 */
public interface MktTrdRepository extends JpaRepository<MktTrd, MktTrdPrimaryKey> {
	/**
	 * dt 보다 크거나 같은 데이터 목록
	 * @param dt
	 * @param sort
	 * @return
	 */
	public List<MktTrd> findByMktCdAndDtGreaterThanEqual(String mktCd, Date dt, Sort sort);
	
	/**
	 * 
	 * @param mktCd
	 * @param dt
	 * @param page
	 * @return
	 */
	public List<MktTrd> findByMktCdAndDtLessThanEqual(String mktCd, Date dt, Pageable page);
}
