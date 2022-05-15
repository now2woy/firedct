package ji.hs.firedct.batch.dart.svc;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.dart.entity.DartHis;
import ji.hs.firedct.data.dart.entity.DartKey;
import ji.hs.firedct.data.dart.entity.DartNotice;
import ji.hs.firedct.data.dart.repository.DartHisRepository;
import ji.hs.firedct.data.dart.repository.DartKeyRepository;
import ji.hs.firedct.data.dart.repository.DartNoticeRepository;
import ji.hs.firedct.data.stock.entity.Itm;
import ji.hs.firedct.data.stock.repository.ItmRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * DART 공시 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class DartNoticeService {
	@Autowired
	private ItmRepository itmRepo;
	
	@Autowired
	private DartKeyRepository dartKeyRepo;
	
	@Autowired
	private DartHisRepository dartHisRepo;
	
	@Autowired
	private DartNoticeRepository dartNoticeRepo;
	
	/**
	 * DART 공시 URL
	 */
	@Value("${constant.url.dartNotice}")
	private String dartNoticeUrl;
	
	/**
	 * 
	 * @param stDt
	 * @param edDt
	 */
	public void crawling(String stDt, String edDt) {
		log.info("{} ~ {} 공시 자료 수집 시작", stDt, edDt);
		
		List<DartKey> keys = dartKeyRepo.findAll();
		AtomicInteger keyIdx = new AtomicInteger(0);
		
		List<Itm> itms = itmRepo.findByDartItmCdIsNotNull(null);
		
		itms.stream().forEach(itm -> {
			
			Optional<DartHis> option = dartHisRepo.findByItmCd(itm.getItmCd());
			
			if(option.isPresent()) {
				log.info("{} 이미 수집된 이력이 있습니다.", itm.getItmCd());
			} else {
				boolean result = save(keys, keyIdx, itm.getItmCd(), itm.getDartItmCd(), stDt, edDt);
				
				if(result) {
					DartHis dartHis = new DartHis();
					dartHis.setItmCd(itm.getItmCd());
					
					// 두번 처리되지 않게 이력에 저장
					dartHisRepo.saveAndFlush(dartHis);
				}
			}
		});
		
		log.info("{} ~ {} 공시 자료 수집 종료", stDt, edDt);
	}
	
	/**
	 * 
	 * @param dartApiKey
	 * @param itmCd
	 * @param stDt
	 * @param edDt
	 * @return
	 */
	private boolean save(final List<DartKey> keys, final AtomicInteger keyIdx, final String itmCd, final String dartItmCd, final String stDt, final String edDt) {
		boolean result = true;
		int idx = 1;
		int lastPageNum = 0;
		List<DartNotice> dartNotices = new ArrayList<>();
		
		do {
			Map<String, Object> map = Utils.jsonParse(getDataFromUrl(keys.get(keyIdx.get()).getDartApiKey(), dartItmCd, stDt, edDt, idx));
			
			// 사용한도를 초과했을 경우
			if("020".equals((String)map.get("status"))) {
				// 키인덱스를 증가한다.
				int temp = keyIdx.incrementAndGet();
				
				// Dart Api Key를 모두 사용했을 경우 종료
				if(keys.size() <= temp) {
					result = false;
					break;
				}
				
			// 정상일 경우
			}else if("000".equals((String)map.get("status"))) {
				// 마지막 페이지 번호를 가져온다.
				lastPageNum = (Integer)map.get("total_page");
				
				List<Map<String, String>> blocks = (ArrayList<Map<String, String>>)map.get("list");
				
				blocks.stream().forEach(block -> {
					DartNotice dartNotice = new DartNotice();
					
					dartNotice.setItmCd(itmCd);
					dartNotice.setNoticeCls(block.get("report_nm").startsWith("[") ? block.get("report_nm").substring(1, block.get("report_nm").indexOf(']')) : "");
					dartNotice.setNoticeNo(block.get("rcept_no"));
					dartNotice.setTitle(block.get("report_nm"));
					dartNotice.setNoticeDt(Utils.dateParse(block.get("rcept_dt")));
					
					dartNotices.add(dartNotice);
				});
				
				idx = idx + 1;
				
			// 데이터가 없을 경우 정상 종료
			} else if("013".equals((String)map.get("status"))) {
				break;
				
			// 그 외엔 일단 오류
			} else {
				log.info("{}, {}", itmCd, (String)map.get("status"));
				result = false;
				break;
			}
		} while(idx <= lastPageNum);
		
		dartNoticeRepo.saveAllAndFlush(dartNotices);
		
		return result;
	}
	
	/**
	 * 
	 * @param dartApiKey
	 * @param itmCd
	 * @param stDt
	 * @param edDt
	 * @param pageNum
	 * @return
	 */
	private String getDataFromUrl(final String dartApiKey, final String dartItmCd, final String stDt, final String edDt, final int pageNum) {
		String result = null;
		InputStream is = null;
		try {
			// 1분에 1000건이 넘으면 24시간 차단을 당하기 때문에 100ms 슬립한다.
			Thread.sleep(100L);
			
			URL url = new URL(dartNoticeUrl + "?crtfc_key=" + dartApiKey + "&corp_code=" + dartItmCd + "&bgn_de=" + stDt + "&end_de=" + edDt + "&page_count=100&page_no=" + pageNum);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			is = conn.getInputStream();
			
			result = IOUtils.toString(is, "UTF-8");
		}catch(Exception e) {
			log.error("", e);
		}finally {
			IOUtils.closeQuietly(is);
		}
		
		return result;
	}
}
