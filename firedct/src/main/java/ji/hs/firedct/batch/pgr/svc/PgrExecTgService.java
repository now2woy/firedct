package ji.hs.firedct.batch.pgr.svc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.PgrExecTg;
import ji.hs.firedct.data.stock.repository.PgrExecTgRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로그램 실행 대상 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class PgrExecTgService {
	
	@Autowired
	private PgrExecTgRepository pgrExecTgRepo;
	
	/**
	 * 최종거래일자부터 현재일자까지 List
	 * 
	 * @param format
	 * @param itmCd
	 * @return
	 */
	public List<String> findMaxDtByItmCd(String pgrCd) {
		final List<String> dateLst = new ArrayList<>();
		final int currDate = Integer.parseInt(Utils.dateFormat(new Date()));
		
		PgrExecTg pgrExecTgs = pgrExecTgRepo.findByPgrCd(pgrCd);
		
		Date lastCrawlingdate = null;
		
		// 조회된 데이터가 없을 경우 2007년 1월 2일 부터 수집
		if(pgrExecTgs == null) {
			lastCrawlingdate = Utils.dateParse("20070102");
			
		// 최종거래일이 있을 경우 사용
		}else {
			lastCrawlingdate = Utils.dateParse(pgrExecTgs.getParam01());
		}
		
		// 현재 일자보다 최종거래일이 작을 경우
		while(currDate > Integer.parseInt(Utils.dateFormat(lastCrawlingdate))) {
			// 최종거래일 + 1 하여 목록에 담는다.
			lastCrawlingdate = DateUtils.addDays(lastCrawlingdate, 1);
			dateLst.add(Utils.dateFormat(lastCrawlingdate));
		}
		
		return dateLst;
	}
	
	/**
	 * 프로그램 실행 대상 저장
	 * @param pgrCd
	 * @param param01
	 * @param param02
	 * @param param03
	 * @param param04
	 * @param param05
	 */
	public void savePgrExecTg(String pgrCd, String param01, String param02, String param03, String param04, String param05) {
		PgrExecTg pgrExecTg = new PgrExecTg();
		pgrExecTg.setPgrCd(pgrCd);
		pgrExecTg.setParam01(param01);
		pgrExecTg.setParam02(param02);
		pgrExecTg.setParam03(param03);
		pgrExecTg.setParam04(param04);
		pgrExecTg.setParam05(param05);
		
		pgrExecTgRepo.saveAndFlush(pgrExecTg);
	}
}
