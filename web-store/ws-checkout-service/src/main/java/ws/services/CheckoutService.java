package ws.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.reactive.function.client.WebClient;

import ws.controller.CheckoutController.BasketItem;
import ws.controller.CheckoutController.BasketItemWithPrice;
import ws.controller.CheckoutController.CheckoutOverview;
import ws.controller.CheckoutController.Recommendation;

public class CheckoutService {

	private Logger logger = Logger.getLogger(CheckoutService.class.getName());


	public CheckoutOverview getOverview(UUID userId, HttpServletRequest req) {
		var cookies = Arrays.asList(req.getCookies());

		var responseBasket = WebClient.create().get()
			.uri(String.format("ws-basket-service/basket/%s", userId.toString()))
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.retrieve()
			.bodyToMono(BasketItem[].class).block();


		var basketItems = Arrays.stream(responseBasket)
			.parallel()
			.map(b -> new BasketItemWithPrice(
				b.itemId(), 
				b.amount(), 
				WebClient.create().get()
					.uri(String.format("ws-pricing-service/prices/%s", b.itemId().toString()))
					.cookies(l -> {
						cookies.forEach(c -> l.add(c.getName(), c.getValue()));
					})
					.retrieve()
					.bodyToMono(BigDecimal.class).block()))
			.toList();


		BigDecimal totalPrice = basketItems.stream()
			.map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.amount())))
			.reduce(BigDecimal.valueOf(0), (b1, b2) -> b1.add(b2));


		var recommendations = Arrays.asList(WebClient.create().get()
			.uri("ws-recommendation-service/recommendation")
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.retrieve()
			.bodyToMono(Recommendation[].class).block());

		return new CheckoutOverview(basketItems, totalPrice, recommendations);
	}


	public void makePurchase(UUID userId, HttpServletRequest req) {
		// Process the whole basket

		// Can improve performance here by caching the overview retrieved above 
		//  (with a quick check that the content of the basket has remained the same)

		var cookies = Arrays.asList(req.getCookies());


		var responseBasket = WebClient.create().get()
			.uri(String.format("ws-basket-service/basket/%s", userId.toString()))
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.retrieve()
			.bodyToMono(BasketItem[].class).block();


		var basketItems = Arrays.stream(responseBasket)
			.parallel()
			.map(b -> new BasketItemWithPrice(
				b.itemId(), 
				b.amount(), 
				WebClient.create().get()
					.uri(String.format("ws-pricing-service/prices/%s", b.itemId().toString()))
					.cookies(l -> {
						cookies.forEach(c -> l.add(c.getName(), c.getValue()));
					})
					.retrieve()
					.bodyToMono(BigDecimal.class).block()))
			.toList();


		// account funds ignored for now
		


		// FIXME no distributed transaction implemented here (yet), assume user profiles behave 'correctly'
		// TODO Minor fix: explicitly specify the bought items to remove from the basket?


		WebClient.create().post()
			.uri("ws-history-service/history/add")
			.bodyValue(new Purchase(userId, basketItems))
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.retrieve()
			.bodyToMono(String.class).block();



		// Clear the basket of the user
		WebClient.create().delete()
			.uri(String.format("ws-basket-service/basket/%s", userId.toString()))
			.cookies(l -> {
				cookies.forEach(c -> l.add(c.getName(), c.getValue()));
			})
			.retrieve()
			.bodyToMono(String.class).block();
		// Ignore the result of the DELETE request...
	}

	private record Purchase(UUID userId, Collection<BasketItemWithPrice> items) {}
}
