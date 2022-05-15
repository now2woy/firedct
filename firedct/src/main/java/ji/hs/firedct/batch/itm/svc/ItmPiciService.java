package ji.hs.firedct.batch.itm.svc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ji.hs.firedct.data.dart.entity.DartNotice;
import ji.hs.firedct.data.dart.repository.DartNoticeRepository;
import ji.hs.firedct.data.stock.entity.ItmPici;
import ji.hs.firedct.data.stock.repository.ItmPiciRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 유상증자 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class ItmPiciService {
	@Autowired
	private DartNoticeRepository dartNoticeRepo;
	
	@Autowired
	private ItmPiciRepository itmPiciRepo;
	
	/**
	 * 유상증자 수집
	 */
	public void crawling() {
		log.info("유상증자 수집 시작");
		
		List<DartNotice> dartNotices = dartNoticeRepo.findAll();
		List<ItmPici> itmPicis = new ArrayList<>();
		
		dartNotices.stream()
			// 유상증자 또는 유무상, 전환사채 문구가 있는것 모두 추출
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("유상증자") != -1 || dartNotice.getTitle().indexOf("전환사채") != -1 || dartNotice.getTitle().indexOf("유무상") != -1))
			
			// 아래 문구가 포함된건 제외
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("[정정명령부과]") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("[기재정정]") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("[첨부추가]") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("[첨부정정]") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("전환사채(해외전환사채포함)발행후만기전사채취득") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("주권관련사채권의취득결정") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("주주명부폐쇄") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("특수관계인") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("투자판단관") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("타법인주식") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("정정명령") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("조회공시") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("가액결정") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("수시공시") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("권리락") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("미발행") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("재매각") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("철회") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("소각") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("소송") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("결과") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("가액") == -1))
			.filter(dartNotice -> (dartNotice.getTitle().indexOf("기타") == -1))
			
			// 정렬
			.sorted(Comparator.comparing(DartNotice::getNoticeDt))
			
			// List에 담는다.
			.forEach(dartNotice -> {
				ItmPici itmPici = new ItmPici();
				
				itmPici.setItmCd(dartNotice.getItmCd());
				itmPici.setNoticeCls(dartNotice.getNoticeCls());
				itmPici.setNoticeDt(dartNotice.getNoticeDt());
				itmPici.setNoticeNo(dartNotice.getNoticeNo());
				itmPici.setTitle(dartNotice.getTitle());
				
				itmPicis.add(itmPici);
			});
		
		// DB에 저장
		itmPiciRepo.saveAllAndFlush(itmPicis);
		
		log.info("유상증자 수집 종료");
	}
}
