package ws.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.UserSession;

@Repository
public interface SessionRepository extends MongoRepository<UserSession, UUID> {
	@Query("{ 'sessionId': ?0 }")
	UserSession findBySessionId(UUID sessionId);

	@Query("{ 'username' : ?0 }")
	UserSession findByUsername(String username);

	List<UserSession> findAll();

	void deleteBySessionId(UUID sessionId);
}
