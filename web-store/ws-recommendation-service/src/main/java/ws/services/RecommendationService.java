package ws.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import ws.controller.RecommendationController.Recommendation;

public class RecommendationService {

	private Logger logger = Logger.getLogger(RecommendationService.class.getName());

	public Collection<Recommendation> generateRecommendations(HttpServletRequest req) {
		var cookies = req.getCookies() == null ? List.<Cookie>of() : Arrays.asList(req.getCookies());
		// Take the three most popular purchased products
		var historyItems = WebClient.create().get()
			.uri("ws-history-service/history/recent?limit=1000")
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.retrieve()
			.bodyToMono(HistoryItem[].class).block();


		return Arrays.stream(historyItems)
			.collect(Collectors.groupingBy(HistoryItem::itemId, Collectors.counting())).entrySet().stream()
			.sorted((e1, e2) -> (int) (e2.getValue() - e1.getValue()))
			.limit(3)
			.map(e -> new Recommendation(e.getKey()))
			.toList();
	}


	public Mono<HistoryAndBasket> getHistoryAndBasket(UUID userId, List<Cookie> cookies) {
		return Mono.zip(
			WebClient.create().get()
				.uri("ws-history-service/history/recent?limit=1000")
				.cookies(l -> {
					cookies.forEach(c -> l.add(c.getName(), c.getValue()));
				})
				.retrieve()
				.bodyToMono(HistoryItem[].class),
			WebClient.create().get()
				.uri(String.format("ws-basket-service/basket/%s", userId.toString()))
				.cookies(l -> {
					cookies.forEach(c -> l.add(c.getName(), c.getValue()));
				})
				.retrieve()
				.bodyToMono(BasketItem[].class),
			HistoryAndBasket::new);
	}


	public Collection<Recommendation> generateRecommendations(UUID userId, HttpServletRequest req) {
		// Look at the contents of the users basket before making recommendations
		var cookies = req.getCookies() == null ? List.<Cookie>of() : Arrays.asList(req.getCookies());

		var items = this.getHistoryAndBasket(userId, cookies).block();
		var basket = Arrays.stream(items.basketItems).collect(Collectors.toMap(i -> i.itemId, i -> i.amount));
		
		// Take the three most popular purchased products, excluding items in the current basket
		return Arrays.stream(items.historyItems)
			.map(HistoryItem::itemId)
			.filter(i -> !basket.containsKey(i))
			.collect(Collectors.groupingBy(i -> i, Collectors.counting())).entrySet().stream()
			.sorted((e1, e2) -> (int) (e2.getValue() - e1.getValue()))
			.limit(3)
			.map(e -> new Recommendation(e.getKey()))
			.toList();
	}


	private record HistoryItem(UUID itemId, Integer amount, BigDecimal unitPrice) {}
	private record BasketItem(UUID itemId, Integer amount) {}
	private record HistoryAndBasket(HistoryItem[] historyItems, BasketItem[] basketItems) {}
}
