package ji.hs.firedct.itm.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 종목 거래 Repository
 * @author now2woy
 *
 */
public interface ItmTrdRepository extends JpaRepository<ItmTrd, ItmTrdPrimaryKey> {
	/**
	 * 종목코드에 해당하는 최종 거래일자 조회
	 * @param itmCd
	 * @return
	 */
	@Query("SELECT MAX(i.dt) AS dt FROM ItmTrd i WHERE i.itmCd = :itmCd")
	public Date findMaxDtByItmCd(@Param("itmCd") String itmCd);
	
	/**
	 * 수집된 자료 중 최종 거래일자 조회
	 * @return
	 */
	@Query("SELECT MAX(i.dt) AS dt FROM ItmTrd i")
	public Date findMaxDt();
	
	/**
	 * 종목코드에 해당하는 거래정보 조회
	 * @param itmCd
	 * @return
	 */
	public List<ItmTrd> findByItmCd(String itmCd);
	
	/**
	 * 일자에 해당하는 거래정보 조회
	 * @param dt
	 * @return
	 */
	@Query("SELECT DISTINCT t FROM ItmTrd t LEFT JOIN FETCH t.itm i LEFT JOIN FETCH i.itmFincStss f WHERE t.dt = :dt AND i.dartItmCd IS NOT NULL")
	public List<ItmTrd> findByDt(@Param("dt") Date dt, Pageable page);
	
	/**
	 * 초단기 이동평균금액 빈값 조회
	 * @param page
	 * @return
	 */
	public List<ItmTrd> findByVsttmMvAvgAmtIsNull(Pageable page);
	
	/**
	 * 단기 이동평균금액 빈값 조회
	 * @param page
	 * @return
	 */
	public List<ItmTrd> findBySttmMvAvgAmtIsNull(Pageable page);
	
	/**
	 * 중기 이동평균금액 빈값 조회
	 * @param page
	 * @return
	 */
	public List<ItmTrd> findByMdtmMvAvgAmtIsNull(Pageable page);
	
	/**
	 * 장기 이동평균금액 빈값 조회
	 * @param page
	 * @return
	 */
	public List<ItmTrd> findByLntmMvAvgAmtIsNull(Pageable page);
	
	/**
	 * 거래회전율 빈값 조회
	 * @param page
	 * @return
	 */
	public List<ItmTrd> findByTrdTnovRtIsNull(Pageable page);
	
	/**
	 * 
	 * @param itmCd
	 * @param dt
	 * @param page
	 * @return
	 */
	public List<ItmTrd> findByItmCdAndDtLessThanEqualOrderByDtDesc(@Param("itmCd") String itmCd, @Param("dt") Date dt, Pageable page);
}
