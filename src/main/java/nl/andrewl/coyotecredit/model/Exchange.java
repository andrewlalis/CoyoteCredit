package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a large collection of users that interact with each other.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Exchange {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The name for this exchange.
	 */
	@Column(nullable = false)
	private String name;

	/**
	 * The primary tradeable that's used by this exchange.
	 */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Tradeable primaryTradeable;

	/**
	 * The set of tradeables that this exchange allows users to interact with.
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "exchange_supported_tradeable",
			joinColumns = @JoinColumn(name = "exchange_id"),
			inverseJoinColumns = @JoinColumn(name = "tradeable_id")
	)
	private Set<Tradeable> supportedTradeables;

	/**
	 * The set of custom tradeables created specifically for use in this exchange.
	 */
	@OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Tradeable> customTradeables;

	/**
	 * The set of accounts that are registered with this exchange.
	 */
	@OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY)
	private Set<Account> accounts;

	public Set<Tradeable> getAllTradeables() {
		Set<Tradeable> s = new HashSet<>();
		s.addAll(getSupportedTradeables());
		s.addAll(getCustomTradeables());
		return s;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Exchange e)) return false;
		return this.getId() != null && e.getId() != null && this.getId().equals(e.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getId());
	}
}
