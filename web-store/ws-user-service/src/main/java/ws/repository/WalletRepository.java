package ws.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ws.domain.Wallet;

// @Repository
public interface WalletRepository extends MongoRepository<Wallet, UUID> {
	@Query("{ 'userId': ?0 }")
	Optional<Wallet> findByUserId(UUID userId);

	List<Wallet> findAll();

	void deleteByUserId(UUID userId);
}
