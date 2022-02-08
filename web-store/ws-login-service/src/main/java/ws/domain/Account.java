package ws.domain;

import java.time.Instant;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import ws.controller.LoginController.AccountInfo;

@Document("accounts")
public class Account {
	@Id
	private String username;
	@Field
	private String hash;
	@Field
	private Date creationDate;


	public Account(AccountInfo info) {
		this(info.username(), info.hash());
	}

	public Account(String username, String hash) {
		this(username, hash, Date.from(Instant.now()));
	}

	@PersistenceConstructor
	public Account(String username, String hash, Date creationDate) {
		this.username = username;
		this.hash = hash;
		this.creationDate = creationDate;
	}


	public String getUsername() {
		return this.username;
	}


	public String getHash() {
		return this.hash;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}
}
