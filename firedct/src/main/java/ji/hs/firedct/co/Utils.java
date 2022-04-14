package ji.hs.firedct.co;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Util 모음
 * @author now2woy
 *
 */
@Slf4j
public class Utils {
	/**
	 * 날자 기본 포멧
	 */
	private static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";
	
	private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;
	
	/**
	 * a + b
	 * CASE : a가 null일 경우 0으로 대입
	 * CASE : b가 null일 경우 0으로 대입
	 * CASE : a와 b가 모두 null일 경우 null 리턴
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		// a와 b 모두 null 일 경우
		if(a == null && b == null){
			return null;
		}
		
		// a가 null일 경우
		if(a == null) {
			a = BigDecimal.ZERO;
		}
		
		// b가 null일 경우
		if(b == null){
			b = BigDecimal.ZERO;
		}
		
		return a.add(b);
	}
	
	/**
	 * a 또는 b가 null 이 아닐 경우 cnt에 1을 더한다.
	 * @param cnt
	 * @param a
	 * @param b
	 * @return
	 */
	public static int addCnt(int cnt, BigDecimal a, BigDecimal b) {
		if(a != null || b != null) {
			return cnt + 1;
		} else {
			return cnt;
		}
	}
	
	/**
	 * a - b or a - c
	 * CASE : a가 null일 경우 null
	 * CASE : a가 null이 아니고 b가 null이 아닐 경우 a - b
	 * CASE : a가 null이 아니고 b가 null이고 c가 null아 아닐 경우 a - c
	 * CASE : 그 외에 null
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static BigDecimal subtract(BigDecimal a, BigDecimal b, BigDecimal c) {
		if(a == null) {
			return null;
		} else {
			if(b == null) {
				if(c == null) {
					return null;
				} else {
					return a.subtract(c);
				}
			} else {
				return a.subtract(b);
			}
		}
	}
	
	/**
	 * a * b
	 * CASE : a 가 null일 경우 null
	 * CASE : b 가 null일 경우 null
	 * 
	 * @param a
	 * @param b
	 * @param scale
	 * @return
	 */
	public static BigDecimal multiply(BigDecimal a, BigDecimal b, int scale) {
		return multiply(a, b, scale, DEFAULT_ROUNDING_MODE);
	}
	
	/**
	 * a * b
	 * CASE : a 가 null일 경우 null
	 * CASE : b 가 null일 경우 null
	 * 
	 * @param a
	 * @param b
	 * @param scale
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal multiply(BigDecimal a, BigDecimal b, int scale, RoundingMode roundingMode) {
		if(a == null || b == null) {
			return null;
		}
		
		return a.multiply(b).setScale(scale, roundingMode);
	}
	
	/**
	 * * a / b
	 * CASE : a가 null일 경우 null
	 * CASE : a가 0일 경우 null
	 * CASE : b가 null일 경우 null
	 * CASE : b가 0일 경우 null
	 * 
	 * @param a
	 * @param b
	 * @param scale
	 * @return
	 */
	public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale) {
		return divide(a, b, scale, DEFAULT_ROUNDING_MODE);
	}
	
	/**
	 * a / b
	 * CASE : a가 null일 경우 null
	 * CASE : a가 0일 경우 null
	 * CASE : b가 null일 경우 null
	 * CASE : b가 0일 경우 null
	 * 
	 * @param a
	 * @param b
	 * @param scale
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale, RoundingMode roundingMode) {
		// null 이거나 0일 경우 나눌 수 없다.
		if(a == null || a.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		
		// null 이거나 0일 경우 나눌 수 없다.
		if(b == null || b.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		
		return a.divide(b, scale, roundingMode);
		
	}
	
	/**
	 * String을 BigDecimal로 변환하여 리턴한다.
	 * CASE : String이 빈값일 경우 null
	 * 
	 * @param str
	 * @return
	 */
	public static BigDecimal stringToBigDecimal(String str) {
		if(StringUtils.isNotEmpty(str)) {
			return new BigDecimal(str);
		} else {
			return null;
		}
	}
	
	/**
	 * date를 기본(yyyyMMdd) format 형식으로 파싱하여 Date형식으로 리턴
	 * @param format
	 * @param date
	 * @return
	 */
	public static Date dateParse(String date) {
		return dateParse(DEFAULT_DATE_FORMAT, date);
	}
	
	/**
	 * date를 format 형식으로 파싱하여 Date형식으로 리턴
	 * @param format
	 * @param date
	 * @return
	 */
	public static Date dateParse(String format, String date) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat(format);
			
			return sdf.parse(date);
			
		}catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * date를 기본(yyyyMMdd) format 형식으로 포멧하여 String형식으로 리턴
	 * @param date
	 * @return
	 */
	public static String dateFormat(Date date) {
		return dateFormat(DEFAULT_DATE_FORMAT, date);
	}
	
	/**
	 * date를 format 형식으로 포멧하여 String형식으로 리턴
	 * @param format
	 * @param date
	 * @return
	 */
	public static String dateFormat(String format, Date date) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat(format);
			
			return sdf.format(date);
			
		}catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * json을 Map형식으로 리턴
	 * @param json
	 * @return
	 */
	public static Map<String, Object> jsonParse(String json){
		Map<String, Object> map = null;
		try {
			final ObjectMapper mapper = new ObjectMapper();
			map = mapper.readValue(json, mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class));
		}catch(Exception e) {
			log.error("", e);
		}
		
		return map;
	}
	
	/**
	 * 압축을 푼다.
	 * @param is
	 * @param destDir
	 * @param charsetName
	 * @throws IOException
	 */
	public static boolean unzip(InputStream is, File destDir, String charsetName) throws IOException {
		ZipArchiveInputStream zis = null;
		ZipArchiveEntry entry = null;
		String name = null;
		File target = null;
		FileOutputStream fos = null;
		boolean isUnzip = true;
		
		try {
			zis = new ZipArchiveInputStream(is, charsetName, false);
			
			while ((entry = zis.getNextZipEntry()) != null){
				name = entry.getName();
				
				target = new File (destDir, name);
				
				if(entry.isDirectory()){
					target.mkdirs();
				} else {
					target.createNewFile();
					
					try {
						fos = new FileOutputStream(target);
						IOUtils.copy(zis, fos);
					}catch(Exception e) {
						isUnzip = false;
						log.error("", e);
					}finally {
						IOUtils.closeQuietly(fos);
					}
				}
			}
		}catch(Exception e) {
			log.error("", e);
			isUnzip = false;
		}finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(fos);
		}
		
		return isUnzip;
	}
}
