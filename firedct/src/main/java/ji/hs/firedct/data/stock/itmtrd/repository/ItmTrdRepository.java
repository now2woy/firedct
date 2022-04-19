package ji.hs.firedct.data.stock.itmtrd.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ji.hs.firedct.data.stock.itmtrd.entity.ItmTrd;
import ji.hs.firedct.data.stock.itmtrd.entity.ItmTrdPrimaryKey;

/**
 * 종목 거래 Repository
 * @author now2woy
 *
 */
public interface ItmTrdRepository extends JpaRepository<ItmTrd, ItmTrdPrimaryKey> {
	
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
	public List<ItmTrd> findByDt(Date dt);
	
	/**
	 * 일자에 해당하는 거래정보 조회
	 * @param dt
	 * @param sort
	 * @return
	 */
	public List<ItmTrd> findByDt(Date dt, Sort sort);
	
	/**
	 * 일자에 해당하고 종목코드와 유사한 거래 정보 조회
	 * @param dt
	 * @param itmCd
	 * @return
	 */
	public List<ItmTrd> findByDtAndItmCdLike(Date dt, String itmCd);
	
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
	
	/**
	 * 수집된 자료 중 최종 거래일자 조회
	 * @return
	 */
	@Query("SELECT MAX(i.dt) AS dt FROM ItmTrd i")
	public Date findMaxDt();
	
	/**
	 * 종목코드에 해당하는 최종 거래일자 조회
	 * @param itmCd
	 * @return
	 */
	@Query("SELECT MAX(i.dt) AS dt FROM ItmTrd i WHERE i.itmCd = :itmCd")
	public Date findMaxDtByItmCd(@Param("itmCd") String itmCd);
	
	/**
	 * 일자에 해당하는 거래정보 조회
	 * @param dt
	 * @return
	 */
	@Query("SELECT DISTINCT t FROM ItmTrd t LEFT JOIN FETCH t.itm i WHERE t.dt = :dt AND i.dartItmCd IS NOT NULL")
	public List<ItmTrd> findByDtAndDartItmCdIsNull(@Param("dt") Date dt, Pageable page);
	
	/**
	 * 365일 최저 PER 조회
	 * @param itmCd
	 * @param dt
	 * @return
	 */
	@Query("SELECT MIN(t.per) FROM ItmTrd t WHERE t.itmCd = :itmCd AND dt >= CONVERT(:dt, DATE)  - 365")
	public BigDecimal findMinPerByItmCdAndDtGreaterThanEqual365(@Param("itmCd") String itmCd, @Param("dt") Date dt);
	
	/**
	 * 365일 최대 PER 조회
	 * @param itmCd
	 * @param dt
	 * @return
	 */
	@Query("SELECT MAX(t.per) FROM ItmTrd t WHERE t.itmCd = :itmCd AND dt >= CONVERT(:dt, DATE)  - 365")
	public BigDecimal findMaxPerByItmCdAndDtGreaterThanEqual365(@Param("itmCd") String itmCd, @Param("dt") Date dt);
	
	/**
	 * 365일 최저 PBR 조회
	 * @param itmCd
	 * @param dt
	 * @return
	 */
	@Query("SELECT MIN(t.pbr) FROM ItmTrd t WHERE t.itmCd = :itmCd AND dt >= CONVERT(:dt, DATE)  - 365")
	public BigDecimal findMinPbrByItmCdAndDtGreaterThanEqual365(@Param("itmCd") String itmCd, @Param("dt") Date dt);
	
	/**
	 * 365일 최대 PBR 조회
	 * @param itmCd
	 * @param dt
	 * @return
	 */
	@Query("SELECT MAX(t.pbr) FROM ItmTrd t WHERE t.itmCd = :itmCd AND dt >= CONVERT(:dt, DATE)  - 365")
	public BigDecimal findMaxPbrByItmCdAndDtGreaterThanEqual365(@Param("itmCd") String itmCd, @Param("dt") Date dt);
	
	/**
	 * 365일 최저 종가 조회
	 * @param itmCd
	 * @param dt
	 * @return
	 */
	@Query("SELECT MIN(t.edAmt) FROM ItmTrd t WHERE t.itmCd = :itmCd AND dt >= CONVERT(:dt, DATE)  - 365")
	public BigDecimal findMinEdAmtByItmCdAndDtGreaterThanEqual365(@Param("itmCd") String itmCd, @Param("dt") Date dt);
	
	/**
	 * 365일 최고 종가 조회
	 * @param itmCd
	 * @param dt
	 * @return
	 */
	@Query("SELECT MAX(t.edAmt) FROM ItmTrd t WHERE t.itmCd = :itmCd AND dt >= CONVERT(:dt, DATE)  - 365")
	public BigDecimal findMaxEdAmtByItmCdAndDtGreaterThanEqual365(@Param("itmCd") String itmCd, @Param("dt") Date dt);
	
	
	/**
	 * 일자에 해당하는 거래정보 조회
	 * @param dt
	 * @return
	 */
	@Query("SELECT t FROM ItmTrd t WHERE t.dt = :dt AND trdAmt > 0 AND per > 0 AND pcr > 0 AND pbr IS NOT NULL AND pcr IS NOT NULL AND per IS NOT NULL AND psr IS NOT NULL AND pbr >= 0.2")
	public List<ItmTrd> findByDtQuery(@Param("dt") Date dt, Pageable page);
}