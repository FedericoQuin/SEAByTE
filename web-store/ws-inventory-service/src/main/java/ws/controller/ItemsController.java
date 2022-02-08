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

import ws.domain.Item;
import ws.service.InventoryService;


@RestController
@RequestMapping("items")
public class ItemsController {
	protected Logger logger = Logger.getLogger(ItemsController.class.getName());

	@Autowired
	private InventoryService service;


	@GetMapping(value="", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Item> stockItems() {
		logger.info("Requested current stock items");
		return service.getAllItems();
	}

	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Item stockItemsById(@PathVariable UUID id) {
		logger.info("Requested stock item by its Id");
		return service.getItemById(id); 
	}    
	
	// @PostMapping(value="/")
	// public String addItem(@RequestBody SimpleItem item) {
	//     service.addItem(item.id(), item.name());
	//     return "OK\n";
	// }

	@PostMapping(value="")
	public String addItemWithoutID(@RequestBody String name) {
		service.addItem(name);
		return "OK\n";
	}

	@PutMapping(value="/{id}")
	public String addItemWithID(@PathVariable UUID id, @RequestBody String name) {
		service.addItem(id, name);
		return "OK\n";
	}

	@DeleteMapping(value="/{id}")
	void deleteItem(@PathVariable UUID id) {
		service.deleteItemById(id);
	}


	@DeleteMapping(value="/")
	void deleteItems() {
		service.deleteAllItems();
	}


	

	/**
	 * An item without any categories, mainly used for testing
	 */
	public record SimpleItem(UUID id, String name) {}
	public record InventoryItem(UUID id, Integer amount) {}
}
