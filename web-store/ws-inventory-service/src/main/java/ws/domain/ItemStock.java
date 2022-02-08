package ws.domain;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection="inventory")
public class ItemStock {

	@Id
	private UUID itemId;

	@Field
	private int amount;



	@PersistenceConstructor
	public ItemStock(UUID itemId, int amount) {
		this.itemId = itemId;
		this.amount = amount;
	}


	public UUID getItemId() {
		return itemId;
	}


	public int getAmount() {
		return amount;
	}



	// public void setupDefaultStock() {
	//     this.stock.put(new Item("Apple"), 10);
	//     this.stock.put(new Item("Pear"), 5);
	//     this.stock.put(new Item("Styrofoam cup"), 1000);
	// }


	// public Map<String, Integer> getStockByName() {
	//     return this.stock.entrySet().stream()
	//         .collect(Collectors.toMap(x -> x.getKey().getName(), x -> x.getValue()));
	// }

	// public Set<Item> getStockItems() {
	//     return this.stock.keySet();
	// }


	// public Map<Item, Integer> getStock() {
	//     return this.stock;
	// }


	// public void addInventoryItem(Item item) {
	//     this.stock.putIfAbsent(item, 0);
	// }

	// // public int generateNewID() {
	// //     return this.stock.keySet().stream()
	// //         .map(InventoryItem::getId)
	// //         .map(uid -> Integer.parseInt(uid.toString()))
	// //         .max(Integer::compare).orElse(0) + 1;
	// // }


	// public void deleteItem(UUID id) {
	//     for (var i : stock.entrySet()) {
	//         if (i.getKey().getId().equals(id)) {
	//             stock.remove(i.getKey());
	//             break;
	//         }
	//     }
	// }
}
