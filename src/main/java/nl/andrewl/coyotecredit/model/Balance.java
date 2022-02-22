package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Represents an account's balance for a certain amount of tradeables in their
 * exchange.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_balance")
@Getter
public class Balance {
	@EmbeddedId
	private BalanceId balanceId;

	@MapsId("accountId")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;

	@MapsId("tradeableId")
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "tradeable_id")
	private Tradeable tradeable;

	@Column(nullable = false, precision = 24, scale = 10)
	@Setter
	private BigDecimal amount;

	public Balance(Account account, Tradeable tradeable, BigDecimal amount) {
		this.balanceId = new BalanceId(tradeable.getId(), account.getId());
		this.account = account;
		this.tradeable = tradeable;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		return o instanceof Balance b && this.getBalanceId().equals(b.getBalanceId());
	}
}
