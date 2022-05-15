package ji.hs.firedct.batch.tactic.svc;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.batch.tactic.dao.TacticVO;
import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.Itm;
import ji.hs.firedct.data.stock.entity.ItmFincSts;
import ji.hs.firedct.data.stock.entity.ItmTrd;
import ji.hs.firedct.data.stock.repository.ItmFincStsRepository;
import ji.hs.firedct.data.stock.repository.ItmRepository;
import ji.hs.firedct.data.stock.repository.ItmTrdRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 전략 0을 구글 스프레드 시트로 출력한다.
 * @author now2woy
 *
 */
@Slf4j
@Service
public class Tactic000Service {
	@Autowired
	private ItmRepository itmRepo;
	
	@Autowired
	private ItmTrdRepository itmTrdRepo;
	
	@Autowired
	private ItmFincStsRepository itmFincStsRepo;
	
	/**
	 * 구글 스크립트(스프레드시트 메크로) URL
	 */
	@Value("${constant.url.googleSheet}")
	private String googleSheetUrl;
	
	/**
	 * 구글 스크립트(스프레드시트 메크로) 김민제 URL
	 */
	@Value("${constant.url.googleSheetKmj}")
	private String googleSheetKmjUrl;
	
	/**
	 * 구글 스프레드 시트로 출력한다.
	 * @param dt
	 */
	public void publishing(String dt) {
		try {
			log.info("{}일 데이터 구글 스프레드 시트로 전송 시작", dt);
			
			List<ItmTrd> itmTrds = itmTrdRepo.findByDt(Utils.dateParse(dt), Sort.by("mktTotAmt").ascending());
			
			if(itmTrds.isEmpty()){
				log.info("조회된 데이터가 없어 구글 스프레드 시트로 데이터 전송 생략");
			}else {
				
				List<TacticVO> tactics = new ArrayList<>();
				
				itmTrds.stream().forEach(itmTrd -> {
					TacticVO tactic = new TacticVO();
					Optional<Itm> itm = itmRepo.findByItmCd(itmTrd.getItmCd());
					List<ItmFincSts> itmFincStss = itmFincStsRepo.findByItmCdAndStdDtLessThanEqual(itmTrd.getItmCd(), Utils.dateParse(dt), PageRequest.of(0, 1, Sort.by("stdDt").descending()));
					
					tactic.setItmCd(itmTrd.getItmCd());
					tactic.setItmNm(itm.get().getItmNm());
					tactic.setMkt(itm.get().getMkt());
					tactic.setEdAmt(itmTrd.getEdAmt());
					tactic.setMktTotAmt(itmTrd.getMktTotAmt());
					tactic.setPbr(itmTrd.getPbr());
					tactic.setPcr(itmTrd.getPcr());
					tactic.setPer(itmTrd.getPer());
					tactic.setPsr(itmTrd.getPsr());
					tactic.setDt(dt);
					tactic.setMinEdAmt(itmTrdRepo.findMinEdAmtByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), Utils.dateParse(dt)));
					tactic.setMaxEdAmt(itmTrdRepo.findMaxEdAmtByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), Utils.dateParse(dt)));
					
					// 매수목표가 생성((365일 최고가 / 150) * 100 = 매수목표가로 구매 후 365일 최고가가 되면 50% 상승)
					tactic.setTgEdAmt(Utils.multiply(Utils.divide(tactic.getMaxEdAmt(), new BigDecimal("150"), 0), new BigDecimal("100"), 0));
					
					if(!itmFincStss.isEmpty()) {
						tactic.setRoe(itmFincStss.get(0).getRoe());
						tactic.setRoa(itmFincStss.get(0).getRoa());
						tactic.setDebtRt(itmFincStss.get(0).getDebtRt());
						tactic.setFscrFst(itmFincStss.get(0).getFscrFst());
						tactic.setFscrSnd(itmFincStss.get(0).getFscrSnd());
						tactic.setFscrTrd(itmFincStss.get(0).getFscrTrd());
					}
					
					tactics.add(tactic);
				});
				
				AtomicInteger i = new AtomicInteger(1);
				
				tactics.stream()
				.sorted(Comparator.comparing(TacticVO::getMktTotAmt))
				.forEach(tactic -> {
					tactic.setSeq(i.getAndIncrement());
				});
				
				Map<String, Object> param = new HashMap<>();
				param.put("SHEET_NM", "데이터");
				param.put("data", tactics);
				
				// 시트로 출력
				callSheet(param, 0);
			}
			
			log.info("{}일 데이터 구글 스프레드 시트로 전송 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 구글 스프레드 시트로 데이터를 전송한다.
	 * @param cls
	 * @param json
	 */
	public void callMacro(String cls, String json) {
		OutputStreamWriter osw = null;
		PrintWriter pw = null;
		try {
			URL url = null;
			
			if("1".equals(cls)) {
				url = new URL(googleSheetUrl);
			}else if("2".equals(cls)) {
				url = new URL(googleSheetKmjUrl);
			}
			
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			
			http.setRequestMethod("POST");
			http.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			http.setDoOutput(true);
			
			osw = new OutputStreamWriter(http.getOutputStream());
			
			pw = new PrintWriter(osw);
			pw.write(json);
			pw.flush();
			IOUtils.closeQuietly(pw);
			http.connect();
			
			String result = IOUtils.toString(http.getInputStream(), "UTF-8");
			
			log.info("{}", result);
			
		}catch(Exception e) {
			log.error("", e);
		}finally {
			IOUtils.closeQuietly(pw);
			IOUtils.closeQuietly(osw);
		}
	}
	
	/**
	 * 넘어온 데이터를 시트로 출력한다.
	 * @param param
	 * @param dmlCd
	 */
	public void callSheet(Map<String, Object> param, int flag) {
		log.info("{} 데이터 삭제 시작", param.get("SHEET_NM"));
		// 삭제 dmlCd를 파라미터에 담는다.
		param.put("dmlCd", "D");
		
		// flag 가 0일 경우 전체 파일에 전송
		if(flag == 0) {
			// 시트에 데이터를 출력 한다.
			callMacro("1", Utils.writeValueAsJson(param));
			callMacro("2", Utils.writeValueAsJson(param));
			
		// flag가 1일 경우 1번 파일에만 전송
		}else if(flag == 1) {
			// 시트에 데이터를 출력 한다.
			callMacro("1", Utils.writeValueAsJson(param));
			
		// flag가 2일 경우 2번 파일에만 전송
		}else if(flag == 2) {
			// 시트에 데이터를 출력 한다.
			callMacro("2", Utils.writeValueAsJson(param));
		}
		
		log.info("{} 데이터 입력 시작", param.get("SHEET_NM"));
		
		// 입력 dmlCd를 파라미터에 담는다.
		param.put("dmlCd", "I");
		
		// flag 가 0일 경우 전체 파일에 전송
		if(flag == 0) {
			// 시트에 데이터를 출력 한다.
			callMacro("1", Utils.writeValueAsJson(param));
			callMacro("2", Utils.writeValueAsJson(param));
			
			// flag가 1일 경우 1번 파일에만 전송
		}else if(flag == 1) {
			// 시트에 데이터를 출력 한다.
			callMacro("1", Utils.writeValueAsJson(param));
			
			// flag가 2일 경우 2번 파일에만 전송
		}else if(flag == 2) {
			// 시트에 데이터를 출력 한다.
			callMacro("2", Utils.writeValueAsJson(param));
		}
	}
}