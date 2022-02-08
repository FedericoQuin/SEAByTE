package ws.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.Item;


@Repository
public interface ItemRepository extends MongoRepository<Item, UUID> {
	@Query("{ 'id': ?0 }")
	Optional<Item> findById(UUID id);

	List<Item> findAll();

	void deleteById(UUID id);
}
