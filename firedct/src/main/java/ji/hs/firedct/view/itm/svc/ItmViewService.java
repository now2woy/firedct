package ji.hs.firedct.view.itm.svc;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.data.stock.entity.ItmTrd;
import ji.hs.firedct.data.stock.repository.ItmFincStsRepository;
import ji.hs.firedct.data.stock.repository.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 종목 화면 출력 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class ItmViewService {
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	@Autowired
	private ItmFincStsRepository itmFincStsRepo;
	
	/**
	 * 수집된 최종일자의 시총금액 작은순으로 정렬하여 100건 조회
	 * 
	 * @param page
	 * @return
	 */
	public List<ItmTrd> allItmTrd(int page){
		Date maxDt = itmTrdRepo.findMaxDt();
		
		List<ItmTrd> itmTrds = itmTrdRepo.findByDtAndDartItmCdIsNull(maxDt, PageRequest.of(page, 100, Sort.by("mktTotAmt").ascending()));
		
		itmTrds.stream().forEach(itmTrd -> {
			itmTrd.setItmFincStss(itmFincStsRepo.findByItmCd(itmTrd.getItmCd(), PageRequest.of(0, 4)));
		});
		
		return itmTrds;
	}
}