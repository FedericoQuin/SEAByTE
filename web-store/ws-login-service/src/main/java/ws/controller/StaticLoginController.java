package ws.controller;


import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class StaticLoginController {
	private Logger logger = Logger.getLogger(StaticLoginController.class.getName());

	@GetMapping(value="/login")
	public String getLoginPage() {
		logger.info("Requesting main login page");
		return "login.html";
	}

	@GetMapping(value="/register")
	public String getRegistrationPage() {
		return "registration.html";
	}
	
}
