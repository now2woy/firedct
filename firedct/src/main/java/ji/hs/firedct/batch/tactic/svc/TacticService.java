package ji.hs.firedct.batch.tactic.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 전략들을 모두 액셀로 전송한다.
 * @author now2woy
 *
 */
@Service
public class TacticService {
	@Autowired
	private Tactic000Service tactic000Service;
	
	@Autowired
	private Tactic020Service tactic020Service;
	
	@Autowired
	private Tactic024Service tactic024Service;
	
	@Autowired
	private Tactic030Service tactic030Service;
	
	@Autowired
	private Tactic901Service tactic901Service;
	
	/**
	 * 모든 전략을 액셀로 전송한다.
	 * @param dt
	 */
	public void publishing(String dt, String flag) {
		if(flag == null) {
			tactic000Service.publishing(dt);
			tactic020Service.publishing(dt);
			tactic024Service.publishing(dt);
			tactic030Service.publishing(dt);
			//tactic901Service.publishing(dt);
			
		} else if("000".equals(flag)) {
			tactic000Service.publishing(dt);
			
		} else if("020".equals(flag)) {
			tactic020Service.publishing(dt);
			
		} else if("024".equals(flag)) {
			tactic024Service.publishing(dt);
			
		} else if("030".equals(flag)) {
			tactic030Service.publishing(dt);
			
		} else if("901".equals(flag)) {
			tactic901Service.publishing(dt);
		}
	}
}
