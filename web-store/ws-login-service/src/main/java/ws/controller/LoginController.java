package ws.controller;

import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ws.domain.Account;
import ws.domain.LoginResult;
import ws.services.LoginService;


@RestController
public class LoginController {
	protected Logger logger = Logger.getLogger(LoginController.class.getName());

	@Autowired
	private LoginService service;


	@PostMapping(value = "/login", produces=MediaType.APPLICATION_JSON_VALUE)
	public LoginResult userLogin(HttpServletResponse response, @RequestParam String username, @RequestParam String hash) {
		logger.info(String.format("Tried to login with username '%s' and hash '%s'", username, hash));
		var res = service.login(username, hash);
		logger.info("Session ID = " + res.cookie().sessionId());

		var sessionCookie = new Cookie("sessionString", res.cookie().sessionId());
		sessionCookie.setMaxAge(-1); 

		var sessionCookieUsername = new Cookie("sessionUsername", username);
		sessionCookieUsername.setMaxAge(-1);
		
		response.addCookie(sessionCookie);
		response.addCookie(sessionCookieUsername);
		return res;
	}

	@GetMapping(value="/accounts/{username}", produces=MediaType.APPLICATION_JSON_VALUE)
	public AccountInfo accountInfo(@PathVariable String username) {
		var acc = service.retrieveAccount(username);
		return new AccountInfo(username, acc.getHash());
	}


	// curl -X POST -d '{"username":"federico","hash":"test"}' -H "Content-Type: application/json" "http://localhost:8080/register"
	@PostMapping(value="/register")
	public String registerNewAccount(@RequestBody AccountInfo info) {
		logger.info(String.format("Registering new account with username '%s' and hash '%s'", info.username, info.hash));
		var res = service.registerNewAccount(new Account(info));
		return res;
	}


	@PutMapping(value="/accounts/{username}")
	public void addAccount(@PathVariable String username, @RequestBody(required = false) String hash) {
		service.registerNewAccount(new Account(username, hash == null ? "" : hash));
	}


	@DeleteMapping(value="/accounts/{username}")
	public void deleteAccount(@PathVariable String username) {
		service.deleteAccountByUsername(username);
	}

	@DeleteMapping(value="/accounts")
	public void deleteAccounts() {
		service.deleteAllAccounts();
	}

	public static record AccountInfo(String username, String hash) {}
}
