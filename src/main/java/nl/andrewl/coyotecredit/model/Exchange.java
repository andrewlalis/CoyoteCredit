package nl.andrewl.coyotecredit.model;

import lombok.Getter;

import javax.persistence.*;
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

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(
			name = "exchange_supported_currency",
			joinColumns = @JoinColumn(name = "currency_id"),
			inverseJoinColumns = @JoinColumn(name = "exchange_id")
	)
	private Set<Currency> supportedCurrencies;
}
