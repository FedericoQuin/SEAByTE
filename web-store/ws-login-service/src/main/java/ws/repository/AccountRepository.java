package ws.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import ws.domain.Account;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
	@Query("{ 'username': ?0 }")
	Account findByUsername(String username);

	List<Account> findAll();

	void deleteById(String username);
}
