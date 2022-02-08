package ws.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Category {
	private String name;
	private List<Category> subCategories;



	public Category(String name) {
		this.name = name;
		this.subCategories = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}


	public void addSubCategory(Category category) {
		this.subCategories.add(category);
	}

	public Optional<List<Category>> getSubCategories() {
		return Optional.ofNullable(subCategories.isEmpty() ? null : subCategories);
	}

}
