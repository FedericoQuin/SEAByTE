package ws.controller;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.services.CheckoutService;



@RestController
@RequestMapping(value="/checkout")
public class CheckoutController {
	protected Logger logger = Logger.getLogger(CheckoutController.class.getName());

	@Autowired
	private CheckoutService checkoutService;
	
	// TODO serve web page here as well


	@GetMapping("/overview")
	public CheckoutOverview getOverview(@CookieValue(name="user-id") UUID userId, HttpServletRequest req) {
		return this.checkoutService.getOverview(userId, req);
	}

	@PostMapping("/buy")
	public void checkout(@CookieValue(name="user-id") UUID userId, HttpServletRequest req) {
		this.checkoutService.makePurchase(userId, req);
	}


	@PostMapping("/clear")
	public void clear(@CookieValue(name="user-id") UUID userId, HttpServletRequest req) {
		this.checkoutService.clearPurchase(userId, req);
	}



	public record Recommendation(UUID itemId) {}
	public record BasketItem(UUID itemId, Integer amount) {}
	public record BasketItemWithPrice(UUID itemId, Integer amount, BigDecimal unitPrice) {}

	public record CheckoutOverview(Collection<BasketItemWithPrice> items, 
		BigDecimal totalPrice,
		Collection<Recommendation> recommendations) {}
}
