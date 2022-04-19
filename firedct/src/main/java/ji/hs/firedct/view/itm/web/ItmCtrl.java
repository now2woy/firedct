package ji.hs.firedct.view.itm.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ji.hs.firedct.data.stock.itmtrd.entity.ItmTrd;
import ji.hs.firedct.view.itm.svc.ItmViewService;

@RestController
@RequestMapping("/api/itms")
public class ItmCtrl {
	@Autowired
	private ItmViewService itmViewService;
	
	@GetMapping("")
	public List<ItmTrd> allItmTrd(@RequestParam(defaultValue = "0") String page){
		return itmViewService.allItmTrd(Integer.parseInt(page));
	}
}
