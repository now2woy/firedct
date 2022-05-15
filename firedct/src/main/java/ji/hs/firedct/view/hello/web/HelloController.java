package ji.hs.firedct.view.hello.web;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ji.hs.firedct.data.stock.entity.Cd;
import ji.hs.firedct.view.cd.svc.CdViewService;

@RestController
public class HelloController {
	@Autowired
	private CdViewService cdViewService;
	
	@GetMapping("/hello")
	public String greeting() {
		
		Optional<Cd> cd = cdViewService.findbyId("00003", "00003");
		
		cd.get().setUseYn("Y");
		
		cdViewService.save(cd.get());
		
		return "Hello World!!!";
	}
}
