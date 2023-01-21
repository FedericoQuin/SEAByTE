package ws.controller;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ws.domain.Purchase.ItemPurchase;
import ws.services.HistoryService;



@RestController
@RequestMapping("history")
public class HistoryController {
	protected Logger logger = Logger.getLogger(HistoryController.class.getName());

	@Autowired
	private HistoryService historyService;
	

	@GetMapping(value="/{userId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Collection<Collection<ItemPurchase>> getOverview(@PathVariable UUID userId) {
		return this.historyService.getAllPurchases(userId);
	}

	@PostMapping(value="/add")
	public void addNewPurchase(@RequestBody PurchaseWithoutId purchase) {
		this.historyService.addNewPurchase(purchase.userId(), purchase.items());
	}


	@GetMapping(value="/recent")
	public Collection<ItemPurchase> getRecentPurchases(@RequestParam(defaultValue="1000") String limit) {
		return this.historyService.getRecentPurchases(Integer.parseInt(limit));
	}


	@DeleteMapping(value="/")
	public void deleteAllPurchases() {
		this.historyService.clearHistory();
	}


	public record PurchaseWithoutId(UUID userId, Collection<ItemPurchase> items) {}
}
