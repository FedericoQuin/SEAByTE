package ws.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.ItemStock;

@Repository
public interface InventoryRepository extends MongoRepository<ItemStock, UUID> {
	@Query("{ 'itemId': ?0 }")
	Optional<ItemStock> findById(UUID id);

	List<ItemStock> findAll();

	void deleteById(UUID id);
}
