package controller;

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


	// @GetMapping("/login")
	// public String getLoginPage() {
	//     logger.info("Requesting main login page");
	//     return "login.html";
	// }

	// @GetMapping("/register")
	// public String getRegistrationPage() {
	//     return "registration.html";
	// }
}
