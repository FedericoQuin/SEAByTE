package ws.services;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import ws.domain.Purchase;
import ws.domain.Purchase.ItemPurchase;
import ws.repository.PurchasesRepository;

public class HistoryService {

	private Logger logger = Logger.getLogger(HistoryService.class.getName());

	@Autowired
	private PurchasesRepository repository;

	public Collection<Collection<ItemPurchase>> getAllPurchases(UUID userId) {
		this.logger.info("Total amount of purchases in history: " + this.repository.findAll().size());
		return this.repository.findByUserId(userId).stream()
			.map(Purchase::getPurchasedItems)
			.toList();
	}


	public void addNewPurchase(UUID userId, Collection<ItemPurchase> items) {
		this.logger.info(String.format(
				"Adding a new purchase containing %d different items for user %s",
				items.size(), userId.toString()));
		this.repository.save(new Purchase(userId, items));
	}


	public Collection<ItemPurchase> getRecentPurchases(int limit) {
		return this.repository.findAll(Sort.by(Sort.Direction.DESC, "purchaseDate")).stream()
			.map(Purchase::getPurchasedItems)
			.flatMap(Collection::stream)
			.limit(limit)
			.toList();
	}
}
