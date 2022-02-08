package ws.domain;

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import ws.repository.SessionRepository;

public class SessionManager {
	


	// TODO: check for expiration of sessions periodically, and remove them from the list of open sessions

	@Autowired
	private SessionRepository repository;

	

	public SessionManager() {}


	public boolean hasSession(UUID sessionId) {
		return this.repository.existsById(sessionId);
	}

	public boolean hasSession(String username) {
		return this.getSession(username).isPresent();
		// return openSessions.stream().anyMatch(s -> s.hasUsername(username));
	}

	public Optional<UserSession> getSession(String username) {
		return Optional.ofNullable(this.repository.findByUsername(username));
		// return openSessions.stream()
		//     .filter(s -> s.hasUsername(username))
		//     .findFirst().get();
	}


	public UUID createSession(String username) {
		UUID newId = this.createNewSessionId();
		this.repository.insert(new UserSession(newId, Date.from(Instant.now()), username));
		return newId;
	}

	
	private UUID createNewSessionId() {
		return UUID.randomUUID();
	}

}
