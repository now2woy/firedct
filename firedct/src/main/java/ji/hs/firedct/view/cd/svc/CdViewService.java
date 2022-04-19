package ji.hs.firedct.view.cd.svc;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ji.hs.firedct.data.stock.cd.entity.Cd;
import ji.hs.firedct.data.stock.cd.entity.CdPrimaryKey;
import ji.hs.firedct.data.stock.cd.repository.CdRepository;

/**
 * 코드 Service
 * @author now2woy
 *
 */
@Service
public class CdViewService {
	@Autowired
	private CdRepository cdRepository;
	
	@Transactional
	public void save(Cd cd) {
		cdRepository.save(cd);
	}
	
	public Optional<Cd> findbyId(String cls, String cd) {
		return cdRepository.findById(new CdPrimaryKey(cls, cd));
	}
}
