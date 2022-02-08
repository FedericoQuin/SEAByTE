package ws.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import ws.services.RecommendationService;



@RestController
@RequestMapping(value="/recommendation")
public class RecommendationController {
	protected Logger logger = Logger.getLogger(RecommendationController.class.getName());

	@Autowired
	private RecommendationService recommendationService;
	
	@GetMapping("")
	public Collection<Recommendation> getRecommendations(@CookieValue(name="user-id", required=false) UUID userId, HttpServletRequest req) {
		this.logger.info("Got cookie " + (userId == null ? "null" : userId.toString()));
		return this.recommendationService.generateRecommendations(req);
	}



	@GetMapping("info/{itemId}")
	public String getInfo(@PathVariable UUID itemId, HttpServletRequest req) {
		var cookies = Arrays.asList(req.getCookies());

		// Proxy call to inventory service
		return WebClient.create().get()
			.uri(String.format("ws-inventory-service/items/%s", itemId.toString()))
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.retrieve()
			.bodyToMono(String.class).block();
	}


	@PostMapping("buy/{itemId}")
	public String buyItem(@CookieValue(name="user-id") String userId, 
			@PathVariable UUID itemId, 
			@RequestParam(name="amount") String amount,
			HttpServletRequest req) {
		var cookies = Arrays.asList(req.getCookies());

		return WebClient.create().post()
			.uri(String.format("ws-basket-service/basket/add"))
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(new ItemPurchase(itemId, Integer.parseInt(amount)))
			.cookie("user-id", userId)
			.retrieve()
			.bodyToMono(String.class).block();
	}



	public record Recommendation(UUID itemId) {}
	private record ItemPurchase(UUID itemId, int amount) {}
}
