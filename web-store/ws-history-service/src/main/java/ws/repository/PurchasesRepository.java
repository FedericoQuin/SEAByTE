package ws.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.Purchase;

@Repository
public interface PurchasesRepository extends MongoRepository<Purchase, UUID> {
	@Query("{ 'purchaseId': ?0 }")
	Optional<Purchase> findById(UUID id);

	@Query("{ 'userId': ?0 }")
	List<Purchase> findByUserId(UUID userId);

	List<Purchase> findAll();

	void deleteById(UUID id);
}
