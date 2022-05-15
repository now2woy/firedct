package ji.hs.firedct.batch.calc;

/**
 * F-SCORE 1번 계산기
 * @author now2woy
 *
 */
public class FscrFstCalc {
	/**
	 * F-SCORE 1번 계산
	 * @param cnt
	 * @return
	 */
	public static String calc(final Long cnt) {
		// 기본은 0
		String result = "0";
		
		// 유상증자 횟수가 없을 경우
		if(cnt == 0L) {
			// 1
			result = "1";
		}
		
		return result;
	}
}
