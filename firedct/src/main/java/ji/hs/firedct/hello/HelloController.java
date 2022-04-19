package ji.hs.firedct.hello;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ji.hs.firedct.cd.svc.CdService;
import ji.hs.firedct.data.stock.cd.entity.Cd;

@RestController
public class HelloController {
	@Autowired
	private CdService cdService;
	
	@GetMapping("/hello")
	public String greeting() {
		
		Optional<Cd> cd = cdService.findbyId("00003", "00003");
		
		cd.get().setUseYn("Y");
		
		cdService.save(cd.get());
		
		return "Hello World!!!";
	}
}
