package ws.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import ws.controller.LoginController;
import ws.controller.StaticLoginController;
import ws.domain.Account;
import ws.domain.LoginResult;
import ws.domain.SessionCookie;
import ws.domain.SessionManager;
import ws.repository.AccountRepository;


@SpringBootApplication(exclude = {
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class
})
@EnableMongoRepositories("ws.repository")
public class LoginService {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "login");
		System.setProperty("registration.server.hostname", "localhost");
		
		SpringApplication.run(LoginService.class, args);
	}


	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private SessionManager sessionManager;


	@Bean
	public LoginController loginController() {
		return new LoginController();
	}

	@Bean
	public StaticLoginController staticLoginController() {
		return new StaticLoginController();
	}

	@Bean
	public SessionManager sessionManger() {
		return new SessionManager();
	}



	public LoginResult login(String username, String hash) {
		if (username == null || hash == null) {
			return new LoginResult(false, null, LoginResult.DEFAULT_FAILURE_MESSAGE);
		}
		// Check existence in account database
		if (!accountRepository.existsById(username)) {
			return new LoginResult(false, null, LoginResult.DEFAULT_FAILURE_MESSAGE);
		}

		
		var result = accountRepository.findByUsername(username);
		if (!result.getHash().equals(hash)) {
			return new LoginResult(false, null, LoginResult.DEFAULT_FAILURE_MESSAGE);
		}

		UUID sessionId; 
		if (sessionManager.hasSession(username)) {
			sessionId = sessionManager.getSession(username).get().getId();
		} else {
			sessionId = sessionManager.createSession(username);
		}
		
		return new LoginResult(true, new SessionCookie(sessionId.toString()), "");
	}


	public String registerNewAccount(Account info) {
		this.accountRepository.insert(info);
		// Add new account to database
		return "Succes!";
	}


	public void deleteAccountByUsername(String username) {
		this.accountRepository.deleteById(username);
	}

	public void deleteAllAccounts() {
		this.accountRepository.deleteAll();
	}


	public Account retrieveAccount(String username) {
		return this.accountRepository.findByUsername(username);
	}
}
