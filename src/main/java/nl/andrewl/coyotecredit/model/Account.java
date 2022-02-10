package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
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

	@Column(nullable = false, unique = true)
	private String number;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Exchange exchange;

	@OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Balance> balances;

	public Map<Currency, BigDecimal> getMappedBalances() {
		Map<Currency, BigDecimal> b = new HashMap<>();
		for (var bal : getBalances()) {
			b.put(bal.getCurrency(), bal.getAmount());
		}
		return b;
	}
}
