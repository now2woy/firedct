package ji.hs.firedct.hello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ji.hs.firedct.itm.dao.Itm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HelloService {
	public void test() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			Map<String, Object> map2 = new HashMap<>();
			map2.put("dmlCd", "I");
			
			List<Itm> itms = new ArrayList<>();
			
			Itm itm01 = new Itm();
			Itm itm02 = new Itm();
			
			itm01.setItmCd("1");
			itm02.setItmCd("2");
			
			itms.add(itm01);
			itms.add(itm02);
			
			map2.put("data", itms);
			
			String json2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map2);
			
			log.info("{}", json2);
			
		}catch(Exception e) {
			log.error("", e);
		}
	}
}
