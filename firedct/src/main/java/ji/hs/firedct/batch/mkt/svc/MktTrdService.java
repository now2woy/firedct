package ji.hs.firedct.batch.mkt.svc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ji.hs.firedct.batch.pgr.svc.PgrExecTgService;
import ji.hs.firedct.co.Utils;
import ji.hs.firedct.data.stock.entity.ItmTrd;
import ji.hs.firedct.data.stock.entity.MktTrd;
import ji.hs.firedct.data.stock.repository.CdRepository;
import ji.hs.firedct.data.stock.repository.MktTrdRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 시장 거래 Service
 * @author now2woy
 *
 */
@Slf4j
@Service
public class MktTrdService {
	@Autowired
	private CdRepository cdRepo;
	
	@Autowired
	private MktTrdRepository mktTrdRepo;
	
	@Autowired
	private PgrExecTgService pgrExecTgService;
	
	/**
	 * 거래소에서 JSON 데이터를 가져오는 URL
	 */
	@Value("${constant.url.krxJson}")
	private String krxJsonUrl;
	
	/**
	 * 시장 거래 수집 프로그램 코드
	 */
	private static final String PGR_CD = "00003";
	
	/**
	 * 시장 거래 정보를 수집한다.
	 */
	public void crawling() {
		List<String> cds = new ArrayList<>();
		// 프로그램 실행 대상 일자 목록 조회
		List<String> dts = pgrExecTgService.findMaxDtByItmCd(PGR_CD);
		
		cds.add("02");
		cds.add("03");
		
		dts.stream().forEach(dt -> {
			log.info("{}일 시장 거래 정보 수집 시작", dt);
			
			List<MktTrd> mktTrds = new ArrayList<>();
			
			cds.stream().forEach(cd -> {
				List<Map<String, String>> blocks = getDataFromUrl(cd, dt);
				
				blocks.stream().forEach(block -> {
					if(!"-".equals(block.get("CLSPRC_IDX"))) {
						if("코스피".equals(block.get("IDX_NM"))
						|| "코스닥".equals(block.get("IDX_NM"))) {
							MktTrd mktTrd = new MktTrd();
							
							mktTrd.setMktCd(cdRepo.findCdByClsAndCdNm("00010", block.get("IDX_NM")));
							mktTrd.setDt(Utils.dateParse(dt));
							mktTrd.setEdAmt(new BigDecimal(block.get("CLSPRC_IDX").replaceAll(",", "")));
							mktTrd.setPer(new BigDecimal(block.get("CLSPRC_IDX").replaceAll(",", "")));
							mktTrd.setPbr(new BigDecimal(block.get("WT_STKPRC_NETASST_RTO").replaceAll(",", "")));
							
							mktTrds.add(mktTrd);
						}
					}
				});
			});
			
			// 데이터 저장
			mktTrdRepo.saveAllAndFlush(mktTrds);
			
			mktTrds.stream().forEach(mktTrd -> {
				List<MktTrd> mvAvgs = mktTrdRepo.findByMktCdAndDtLessThanEqual(mktTrd.getMktCd(), mktTrd.getDt(), PageRequest.of(0, 120, Sort.by("dt").descending()));
				
				mktTrd.setVal01(createMvAvg(mvAvgs, 5));
				mktTrd.setVal02(createMvAvg(mvAvgs, 20));
				mktTrd.setVal03(createMvAvg(mvAvgs, 60));
				mktTrd.setVal04(createMvAvg(mvAvgs, 120));
			});
			
			// 데이터 저장
			mktTrdRepo.saveAllAndFlush(mktTrds);
			
			// 프로그램 실행 대상 저장
			pgrExecTgService.savePgrExecTg(PGR_CD, dt, null, null, null, null);
			
			log.info("{}일 시장 거래 정보 수집 종료", dt);
		});
	}
	
	/**
	 * URL로 부터 데이터를 가져온다.
	 * @param cd
	 * @param dt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, String>> getDataFromUrl(String cd, String dt){
		Document doc = null;
		try {
			doc = Jsoup.connect(krxJsonUrl)
					.data("bld", "dbms/MDC/STAT/standard/MDCSTAT00701")
					.data("idxIndMidclssCd", cd)
					.data("trdDd", dt)
					.get();
			
		}catch(Exception e) {
			log.error("", e);
		}
		
		return (List<Map<String, String>>)Utils.jsonParse(doc.text()).get("output");
	}
	
	/**
	 * scale 만큼 평균을 구한다.
	 * 
	 * @param itmTrd
	 * @param scale
	 * @return
	 */
	private BigDecimal createMvAvg(List<MktTrd> mktTrds, int scale) {
		Map<String, BigDecimal> data = new HashMap<>();
		
		List<MktTrd> lst = mktTrds.stream().limit(scale).collect(Collectors.toList());
		
		if(lst.size() == scale) {
			lst.stream().forEach(mktTrd -> {
				data.put("MV_AVG_AMT", Utils.add(data.get("MV_AVG_AMT"), mktTrd.getPbr()));
			});
		}
		
		return Utils.divide(data.get("MV_AVG_AMT"), new BigDecimal(scale), 2);
	}
}
