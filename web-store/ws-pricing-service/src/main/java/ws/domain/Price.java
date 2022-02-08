package ws.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection="prices")
public class Price {

	@Id
	UUID itemId;

	@Field
	BigDecimal price;



	@PersistenceConstructor
	public Price(UUID itemId, BigDecimal price) {
		this.itemId = itemId;
		this.price = price;
	}


	public UUID getId() {
		return this.itemId;
	}


	public BigDecimal getPrice() {
		return this.price;
	}


	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	

}

