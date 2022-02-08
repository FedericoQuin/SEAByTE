package ws.controller;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.service.BasketService;

@RestController
@RequestMapping("basket")
public class BasketController {

	protected Logger logger = Logger.getLogger(BasketController.class.getName());


	@Autowired
	private BasketService service;



	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Collection<BasketItem> getBasket(@PathVariable UUID id) {
		logger.info(String.format("Requested basket of user with ID=%s", id.toString()));
		return service.getBasketItemsAsCollection(id);
	}

	@PostMapping(value="/add", produces=MediaType.APPLICATION_JSON_VALUE)
	public void addItemToBasket(@CookieValue(name="user-id") UUID id, @RequestBody(required = false) BasketItem item) {
		if (item == null) {
			service.addBasket(id);
		} else {
			service.addItemToBasket(id, item.itemId(), item.amount());
		}
	}


	@DeleteMapping(value="/{id}")
	public void clearBasket(@PathVariable UUID id) {
		service.clearBasket(id);
	}


	@DeleteMapping(value="/removeItem")
	public void deleteItemFromBasket(@CookieValue(name="user-id") UUID id, @RequestBody UUID itemId) {
		service.deleteItemFromBasket(id, itemId);
	}


	public record BasketItem(UUID itemId, Integer amount) {}
}
