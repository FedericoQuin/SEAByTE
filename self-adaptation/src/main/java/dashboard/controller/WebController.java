package dashboard.controller;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebController {

	protected Logger logger = Logger.getLogger(WebController.class.getName());


	@GetMapping("/")
	public ModelAndView mainPage(ModelMap model) {
		return new ModelAndView("forward:/index", model);
	}

	@GetMapping(value="/index")
	public String indexPage() {
		logger.info("Requested main index page");
		return "index.html";
	}
}
