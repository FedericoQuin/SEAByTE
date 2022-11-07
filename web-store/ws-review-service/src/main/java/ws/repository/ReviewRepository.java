package ws.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, UUID> {
	@Query("{ 'reviewId': ?0 }")
	Optional<Review> findById(UUID id);

	List<Review> findAll();

	void deleteById(UUID id);

}
