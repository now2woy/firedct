package ji.hs.firedct.itmtrd.svc;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.itmtrd.dao.ItmTrd;
import ji.hs.firedct.itmtrd.dao.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ItmTrdService {
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	public void tmpDt() {
		final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		final Pageable page = PageRequest.of(0, 5000, Sort.Direction.ASC, "itmCd");
		
		Page<ItmTrd> itmLst = itmTrdRepo.findByTmpDtIsNull(page);
		
		itmLst.stream().forEach(itm -> {
			try {
				itm.setTmpDt(format.parse(itm.getDt()));
			} catch (ParseException e) {
				log.error("", e);
			}
		});
		
		itmTrdRepo.saveAllAndFlush(itmLst);
		
		log.info("처리완료");
	}
}
