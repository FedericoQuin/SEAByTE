package ws.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.UserBasket;

@Repository
public interface BasketRepository extends MongoRepository<UserBasket, UUID> {
	@Query("{ 'userId': ?0 }")
	Optional<UserBasket> findById(UUID id);

	List<UserBasket> findAll();

	void deleteById(UUID id);
}
