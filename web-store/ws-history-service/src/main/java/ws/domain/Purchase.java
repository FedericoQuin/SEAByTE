package ws.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection="history")
public class Purchase {
	
	@Id
	UUID purchaseId;

	@Field
	UUID userId;

	@Field
	Collection<ItemPurchase> purchasedItems;

	@Field
	Date purchaseDate;


	@PersistenceConstructor
	public Purchase(UUID userId, Collection<ItemPurchase> purchasedItems) {
		this.purchaseId = UUID.randomUUID();
		this.userId = userId;
		this.purchasedItems = purchasedItems;
		this.purchaseDate = Date.from(Instant.now());
	}


	public UUID getPurchaseId() {
		return this.purchaseId;
	}

	public UUID getUserId() {
		return this.userId;
	}

	public Collection<ItemPurchase> getPurchasedItems() {
		return this.purchasedItems;
	}

	public Date getPurchaseDate() {
		return this.purchaseDate;
	}



	public record ItemPurchase(UUID itemId, Integer amount, BigDecimal unitPrice) {}
}
