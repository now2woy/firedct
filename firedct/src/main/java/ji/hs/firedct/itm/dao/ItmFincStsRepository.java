package ji.hs.firedct.itm.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 종목 재무제표 Repository
 * @author now2woy
 *
 */
public interface ItmFincStsRepository extends JpaRepository<ItmFincSts, ItmFincStsPrimaryKey> {
	/**
	 * 종목코드 조회
	 * @param itmCd
	 * @param page
	 * @return
	 */
	public List<ItmFincSts> findByItmCd(String itmCd, Pageable page);
	
	/**
	 * 종목코드, 년도, 분기 조회
	 * @param itmCd
	 * @param yr
	 * @param qt
	 * @return
	 */
	public Optional<ItmFincSts> findByItmCdAndYrAndQt(String itmCd, String yr, String qt);
	
	/**
	 * 종목코드, 년도가 같고, 분기보다 작은 값 조회
	 * @param itmCd
	 * @param yr
	 * @param qt
	 * @return
	 */
	public List<ItmFincSts> findByItmCdAndYrAndQtLessThan(String itmCd, String yr, String qt);
	
	/**
	 * 종목코드에 해당하고 기준일자보다 작은 값 조회
	 * @param itmCd
	 * @param stdDt
	 * @param page
	 * @return
	 */
	public List<ItmFincSts> findByItmCdAndStdDtLessThan(String itmCd, Date stdDt, Pageable page);
	
	/**
	 * 종목코드에 해당하고 기준일자보다 작은 값 조회
	 * @param itmCd
	 * @param stdDt
	 * @param page
	 * @return
	 */
	public List<ItmFincSts> findByItmCdAndStdDtLessThanEqual(String itmCd, Date stdDt, Pageable page);
}
