package ws.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import ws.controller.BasketController.BasketItem;
import ws.domain.UserBasket;
import ws.repository.BasketRepository;


public class BasketService {

	private Logger logger = Logger.getLogger(BasketService.class.getName());


	@Autowired
	private BasketRepository repository;


	
	public void addBasket(UUID userId) {
		this.repository.save(new UserBasket(userId));
	}

	public UserBasket getUserBasket(UUID userId) {
		return this.getUserBasket(userId, true).get();
		// return this.repository.findById(userId).orElseGet(() -> {
		// 	UserBasket basket = new UserBasket(userId);
		// 	this.repository.save(basket);
		// 	return basket;
		// });
	}

	public Optional<UserBasket> getUserBasket(UUID userId, boolean initializeIfAbsent) {
		var userBasket = this.repository.findById(userId);

		if (userBasket.isEmpty() && initializeIfAbsent) {
			UserBasket basket = new UserBasket(userId);
			this.repository.save(basket);
			userBasket = Optional.of(basket);
		}

		return userBasket;
		
		// return this.repository.findById(userId).orElseGet(() -> {
		// 	if (!initializeIfAbsent) {
		// 		return Optional.empty();
		// 	}
		// 	UserBasket basket = new UserBasket(userId);
		// 	this.repository.save(basket);
		// 	return basket;
		// }));
	}

	public Collection<BasketItem> getBasketItemsAsCollection(UUID userId) {
		return this.getUserBasket(userId).getBasket().entrySet().stream()
			.map(e -> new BasketItem(e.getKey(), e.getValue()))
			.toList();
	}


	public void addItemToBasket(UUID userId, UUID itemId, int amount) {
		var basketItem = this.getUserBasket(userId);
		basketItem.addItem(itemId, amount);
		this.repository.save(basketItem);
	}

	public boolean hasItemInBasket(UUID userId, UUID itemId) {
		return this.getUserBasket(userId).getItems().contains(itemId);
	}


	public Map<UUID, Integer> getBasketItems(UUID userId) { 
		return this.getUserBasket(userId).getBasket();
	}


	public void deleteItemFromBasket(UUID userId, UUID itemId) {
		var basket = this.getUserBasket(userId);
		basket.removeItem(itemId);
	}


	public void clearBasket(UUID userId) {
		this.getUserBasket(userId, false).ifPresent(b -> {
			b.removeItems();
			this.repository.save(b);
		});
	}

}





