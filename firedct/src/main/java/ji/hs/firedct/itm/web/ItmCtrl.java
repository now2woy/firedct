package ji.hs.firedct.itm.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ji.hs.firedct.itm.dao.ItmTrd;
import ji.hs.firedct.itm.svc.ItmService;
import ji.hs.firedct.itm.svc.ItmTrdService;

@RestController
@RequestMapping("/api/itms")
public class ItmCtrl {
	@Autowired
	private ItmService itmService;
	
	@Autowired
	private ItmTrdService itmTrdService;
	
	@GetMapping("")
	public List<ItmTrd> allItmTrd(@RequestParam(defaultValue = "0") String page){
		return itmTrdService.allItmTrd(Integer.parseInt(page));
	}
}
