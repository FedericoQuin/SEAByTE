package ws.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.Price;


@Repository
public interface PriceRepository extends MongoRepository<Price, UUID> {
	@Query("{ 'itemId': ?0 }")
	Optional<Price> findById(UUID id);

	List<Price> findAll();

	void deleteById(UUID id);
}
