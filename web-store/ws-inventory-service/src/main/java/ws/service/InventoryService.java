package ws.service;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import ws.controller.ItemsController.InventoryItem;
import ws.controller.StockController.StockItem;
import ws.domain.Item;
import ws.domain.ItemStock;
import ws.repository.InventoryRepository;
import ws.repository.ItemRepository;


public class InventoryService {

	private Logger logger = Logger.getLogger(InventoryService.class.getName());


	@Autowired
	private InventoryRepository inventory;

	@Autowired
	private ItemRepository items;


	
	public void initializeDatabaseWithTestData() {
		items.deleteAll();
		inventory.deleteAll();
		
		items.save(new Item("Styrofoam cup", UUID.fromString("1be7d946-99c8-48d5-8e62-e6355369c8f5")));
		items.save(new Item("Apple", UUID.fromString("4b435c1d-7c07-4d36-84ab-72bfee827b86")));
		items.save(new Item("Pear", UUID.fromString("630fe486-6838-45f8-9ede-2eb913992025")));
		items.save(new Item("Plastic bag", UUID.fromString("75d6531d-c14a-4c8b-857b-1129e90fcee7")));
		
		inventory.save(new ItemStock(UUID.fromString("1be7d946-99c8-48d5-8e62-e6355369c8f5"), 1000));
		inventory.save(new ItemStock(UUID.fromString("4b435c1d-7c07-4d36-84ab-72bfee827b86"), 20));
		inventory.save(new ItemStock(UUID.fromString("630fe486-6838-45f8-9ede-2eb913992025"), 100));
		inventory.save(new ItemStock(UUID.fromString("75d6531d-c14a-4c8b-857b-1129e90fcee7"), 5));
		
		var x = items.findAll();
		logger.info("Items saved in the database:");
		x.forEach(i -> logger.info("\t" + i.getName()));
	}
	

	public List<StockItem> getEntireStock() {
		return this.items.findAll().stream()
			.map(i -> new StockItem(i, this.inventory.findById(i.getId()).orElse(new ItemStock(null, 0)).getAmount()))
			.collect(Collectors.toList());
	}


	public List<Item> getAllItems() {
		return this.items.findAll();
	}




	public Item getItemById(UUID id) {
		return this.items.findById(id).orElseThrow();
	}

	public ItemStock getItemStockById(UUID id) {
		return this.inventory.findById(id).orElseThrow();
	}




	public void addItem(UUID id, String name) {
		items.save(new Item(name, id));
	}

	public void addItem(String name) {
		items.save(new Item(name));
	}


	public void addItemStock(UUID id, int amount) {
		this.addItemStock(new InventoryItem(id, amount));
	}

	public void addItemStock(InventoryItem inventoryItem) {
		logger.info("Trying to add stock of an item.");

		if (this.items.findById(inventoryItem.id()).isEmpty()) {
			throw new RuntimeException("Cannot add stock of an item that does not exist in the system.");
		}

		this.inventory.findById(inventoryItem.id()).ifPresentOrElse(i -> {
			logger.info("Found inventory item with same idea, replace amounts.");
			int newAmount = i.getAmount() + inventoryItem.amount();
			this.inventory.deleteById(i.getItemId());
			this.inventory.save(new ItemStock(i.getItemId(), newAmount));
		}, () -> {
			this.inventory.save(new ItemStock(inventoryItem.id(), inventoryItem.amount()));
		});
	}



	public void deleteItemById(UUID id) {
		this.items.deleteById(id);
	}

	public void deleteAllItems() {
		this.items.deleteAll();
	}

	public void deleteAllStock() {
		this.inventory.deleteAll();
	}

}





