package ws.controller;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.domain.Wallet;
import ws.repository.WalletRepository;
import ws.service.UserService;

@RestController
@RequestMapping("wallet")
public class WalletController {

	private Logger logger = Logger.getLogger(WalletController.class.getName());


	@Autowired
	private UserService service;

	@Autowired
	private WalletRepository walletRepository;


	@GetMapping("/funds")
	private BigDecimal retrieveFunds(@CookieValue(value="sessionString", defaultValue="") String sessionString) {
		if (sessionString.isEmpty()) {
			throw new RuntimeException("Not logged in!");
		}
		logger.info(String.format("Requested to retrieve funds from account with session Id %s", sessionString));
		return service.getFundsForAccount(sessionString);
	}


	@PostMapping("/funds")
	private void setFunds(@RequestBody UUID userId, @RequestBody BigDecimal amount) {
		walletRepository.findByUserId(userId).ifPresentOrElse(w -> {
			walletRepository.delete(w);
			w.setBalance(amount);
			walletRepository.save(w);
		}, () -> walletRepository.insert(new Wallet(userId, amount)));
	}
}
