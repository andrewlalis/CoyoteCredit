package nl.andrewl.coyotecredit.model;

import lombok.Getter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Represents a pair of currencies that can be exchanged at a set exchange rate.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"from_currency_id", "to_currency_id", "exchange_id"}))
@Getter
public class ExchangePair {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private Exchange exchange;

	@ManyToOne(optional = false)
	private Currency fromCurrency;

	@ManyToOne(optional = false)
	private Currency toCurrency;

	@Column(nullable = false, precision = 24, scale = 10)
	private BigDecimal exchangeRate = new BigDecimal("1.0");
}
