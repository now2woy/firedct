package ji.hs.firedct.co;

/**
 * 상수
 * @author now2woy
 *
 */
public class Constant {
	/**
	 * KRX Json을 가져오는 URL
	 */
	public static final String KRX_JSON_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
	
	/**
	 * DART 종목코드 파일 URL
	 */
	public static final String DART_CORP_CD_URL = "https://opendart.fss.or.kr/api/corpCode.xml";
	
	/**
	 * DART 종목 / 년도 / 분기 재무제표 URL
	 */
	public static final String DART_FINC_STS_URL = "https://opendart.fss.or.kr/api/fnlttSinglAcntAll.json";
	
	/**
	 * 파일 다운로드 경로
	 */
	public static final String ENV_DOWNLOAD_PATH = "/Downloads";
}
