package ji.hs.firedct.batch.tactic.svc;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.itm.entity.Itm;
import ji.hs.firedct.data.stock.itm.repository.ItmRepository;
import ji.hs.firedct.data.stock.itmtrd.entity.ItmTrd;
import ji.hs.firedct.data.stock.itmtrd.repository.ItmTrdRepository;
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
	
	/**
	 * 구글 스크립트(스프레드시트 메크로) URL
	 */
	@Value("${constant.url.googleSheet}")
	private String googleSheetUrl;
	
	/**
	 * 구글 스프레드 시트로 출력한다.
	 * @param dt
	 */
	public void publishing(String dt) {
		try {
			log.info("{}일 데이터 구글 스프레드 시트로 전송 시작", dt);
			
			Map<String, Object> param = new HashMap<>();
			param.put("SHEET_NM", "데이터");
			param.put("dmlCd", "D");
			
			// DML 코드를 'D'로 넘겨서 데이터를 초기화 한다.
			callMacro(Utils.writeValueAsJson(param));
			
			param.remove("dmlCd");
			param.put("dmlCd", "I");
			
			final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			final Date parseDt = format.parse(dt);
			
			List<ItmTrd> itmTrds = itmTrdRepo.findByDt(parseDt);
			
			itmTrds.stream().forEach(itmTrd -> {
				Optional<Itm> itm = itmRepo.findByItmCd(itmTrd.getItmCd());
				itmTrd.setItmNm(itm.get().getItmNm());
				itmTrd.setMinPer(itmTrdRepo.findMinPerByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), itmTrd.getDt()));
				itmTrd.setMaxPer(itmTrdRepo.findMaxPerByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), itmTrd.getDt()));
				itmTrd.setMinPbr(itmTrdRepo.findMinPbrByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), itmTrd.getDt()));
				itmTrd.setMaxPbr(itmTrdRepo.findMaxPbrByItmCdAndDtGreaterThanEqual365(itmTrd.getItmCd(), itmTrd.getDt()));
			});
			
			param.put("data", itmTrds);
			
			// 새로운 데이터를 넘긴다.
			callMacro(Utils.writeValueAsJson(param));
			
			log.info("{}일 데이터 구글 스프레드 시트로 전송 종료", dt);
		}catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 구글 스프레드 시트로 데이터를 전송한다.
	 * @param json
	 */
	public void callMacro(String json) {
		OutputStreamWriter osw = null;
		PrintWriter pw = null;
		try {
			URL url = new URL(googleSheetUrl);
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
}