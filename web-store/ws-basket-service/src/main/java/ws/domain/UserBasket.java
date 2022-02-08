package ws.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection="basket")
public class UserBasket {

	@Id
	private UUID userId;

	@Field
	private Map<UUID, Integer> items;



	@PersistenceConstructor
	public UserBasket(UUID userId) {
		this.userId = userId;
		this.items = new HashMap<>();
	}


	public UUID getUserId() {
		return userId;
	}


	public Set<UUID> getItems() {
		return items.keySet();
	}


	public void addItem(UUID itemId, int amount) {
		this.items.put(itemId, (this.items.containsKey(itemId) ? this.items.get(itemId) : 0) + amount);
	}

	public Map<UUID, Integer> getBasket() {
		return this.items;
	}


	public void removeItem(UUID itemId) {
		if (this.items.containsKey(itemId)) {
			this.items.remove(itemId);
		}
	}

	public void removeItems() {
		this.items.clear();
	}

}
