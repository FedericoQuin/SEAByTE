package ws.domain;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("sessions")
public class UserSession {
	
	@Id
	private UUID sessionId;

	@Field
	private Date expirationDate;
	
	@Field
	private String username;


	@PersistenceConstructor
	public UserSession(UUID sessionId, Date expirationDate, String username) {
		this.sessionId = sessionId;
		this.expirationDate = expirationDate;
		this.username = username;
	}
	
	public boolean hasId(UUID id) {
		return sessionId.equals(id);
	}


	public boolean hasUsername(String username) {
		return this.username.equals(username);
	}


	public UUID getId() {
		return this.sessionId;
	}

}
