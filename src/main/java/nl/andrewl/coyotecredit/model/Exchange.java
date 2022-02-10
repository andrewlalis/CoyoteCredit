package nl.andrewl.coyotecredit.model;

import lombok.Getter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a large collection of users that interact with each other.
 */
@Entity
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

	@OneToMany(mappedBy = "exchange")
	private Set<ExchangePair> currencyPairs;

	public Set<Currency> getSupportedCurrencies() {
		Set<Currency> currencies = new HashSet<>();
		for (var pair : getCurrencyPairs()) {
			currencies.add(pair.getFromCurrency());
			currencies.add(pair.getToCurrency());
		}
		return currencies;
	}
}
