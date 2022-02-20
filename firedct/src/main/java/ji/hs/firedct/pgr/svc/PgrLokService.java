package ji.hs.firedct.pgr.svc;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ji.hs.firedct.pgr.dao.PgrLok;
import ji.hs.firedct.pgr.dao.PgrLokRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로그램 잠금 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class PgrLokService {
	@Autowired
	private PgrLokRepository pgrLokRepo;
	
	/**
	 * 선택된 코드의 잠금 여부를 가져온다.
	 * @param pgrCd
	 * @return
	 */
	public boolean getLokYn(String pgrCd) {
		boolean result = false;
		Optional<PgrLok> pgrLok = pgrLokRepo.findByPgrCd(pgrCd);
		
		if(pgrLok.isPresent()) {
			if("N".equals(pgrLok.get().getLokIngYn())) {
				result = true;
				
				// 잠금중여부를 "Y" 로 변경
				pgrLok.get().setLokIngYn("Y");
				
				// DB에 입력
				pgrLokRepo.saveAndFlush(pgrLok.get());
			}
		}
		
		return result;
	}
	
	/**
	 * 선택된 코드의 잠금을 해제 한다.
	 * @param pgrCd
	 */
	public void unLok(String pgrCd) {
		PgrLok pgrLok = new PgrLok();
		
		pgrLok.setPgrCd(pgrCd);
		
		// 잠금중여부를 "N" 로 변경
		pgrLok.setLokIngYn("N");
		
		// DB에 입력
		pgrLokRepo.saveAndFlush(pgrLok);
	}
}
