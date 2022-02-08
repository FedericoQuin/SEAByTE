package ws.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Wallet {
	private UUID userId;
	private BigDecimal balance;


	public Wallet(UUID userId, BigDecimal balance) {
		this.userId = userId;
		this.balance = balance;
	}

	public Wallet(UUID userId) {
		this(userId, BigDecimal.ZERO);
	}


	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public UUID getUserId() {
		return this.userId;
	}

	public BigDecimal getBalance() {
		return this.balance;
	}
}
