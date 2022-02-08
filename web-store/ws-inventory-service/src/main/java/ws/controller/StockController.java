package ws.controller;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.controller.ItemsController.InventoryItem;
import ws.domain.Item;
import ws.domain.ItemStock;
import ws.service.InventoryService;

@RestController
@RequestMapping("stock")
public class StockController {

	protected Logger logger = Logger.getLogger(StockController.class.getName());


	@Autowired
	private InventoryService service;



	@GetMapping(value="", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<StockItem> stock() {
		logger.info("Requested current stock");
		return service.getEntireStock();
	}

	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ItemStock stockInventoryById(@PathVariable UUID id) {
		return service.getItemStockById(id);
	}


	// curl -X POST -d '{"id": "1be7d946-99c8-48d5-8e62-e6355369c8f5", "amount": 200}' -H "Content-Type: application/json" localhost:30002/stock
	@PostMapping(value="")
	public void addInventoryItem(@RequestBody InventoryItem inventoryItem) {
		service.addItemStock(inventoryItem);
	}

	@PutMapping(value="/{id}")
	public void addInventoryItem(@PathVariable UUID id, @RequestBody int amount) {
		service.addItemStock(id, amount);
	}

	@DeleteMapping(value="")
	public void deleteStock() {
		service.deleteAllStock();
	}


	public record StockItem(Item item, Integer amount) {}

	
}
