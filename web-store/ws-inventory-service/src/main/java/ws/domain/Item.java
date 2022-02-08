package ws.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="items")
public class Item {
	@Id
	UUID id;

	@Field
	String name;

	@Field
	List<Category> categories;

	public Item(String name, Category... categories) {
		this(name, UUID.randomUUID(), List.of(categories));
	}

	public Item(String name, UUID id, Category... categories) {
		this(name, id, List.of(categories));
	}

	@PersistenceConstructor
	public Item(String name, UUID id, List<Category> categories) {
		this.name = name;
		this.id = id;
		this.categories = categories;
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}
