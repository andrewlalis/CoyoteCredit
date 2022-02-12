package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an account that a user has in an exchange.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"number", "exchange_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The unique account number.
	 */
	@Column(nullable = false, unique = true)
	private String number;

	/**
	 * The user that this account belongs to.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private User user;

	/**
	 * The name on this account.
	 */
	@Column
	private String name;

	/**
	 * The exchange that this account belongs to.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Exchange exchange;

	/**
	 * Whether this account is an administrator in the exchange it's linked to.
	 * Administrators have special permissions to add and remove other accounts,
	 * custom tradeables, exchange rates, and more.
	 */
	@Column(nullable = false)
	private boolean admin;

	/**
	 * The set of tradeable balances that this account has.
	 */
	@OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Balance> balances;

	public Account(String number, User user, String name, Exchange exchange) {
		this.number = number;
		this.user = user;
		this.name = name;
		this.exchange = exchange;
		this.balances = new HashSet<>();
	}

	public Map<Tradeable, BigDecimal> getMappedBalances() {
		Map<Tradeable, BigDecimal> b = new HashMap<>();
		for (var bal : getBalances()) {
			b.put(bal.getTradeable(), bal.getAmount());
		}
		return b;
	}

	public Balance getBalanceForTradeable(Tradeable t) {
		for (var bal : getBalances()) {
			if (bal.getTradeable().equals(t)) return bal;
		}
		return null;
	}

	public BigDecimal getTotalBalance() {
		BigDecimal totalUsd = new BigDecimal(0);
		for (var bal : getBalances()) {
			totalUsd = totalUsd.add(bal.getTradeable().getMarketPriceUsd().multiply(bal.getAmount()));
		}
		return totalUsd.divide(getExchange().getPrimaryTradeable().getMarketPriceUsd(), RoundingMode.HALF_UP);
	}
}
