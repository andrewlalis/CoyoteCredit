package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a type of currency. This can be an actual fiat currency, or
 * perhaps a cryptocurrency, or stocks, or really anything tradeable on the
 * exchange.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Tradeable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String symbol;

	@Enumerated(EnumType.STRING)
	private TradeableType type;

	@Column(nullable = false, precision = 24, scale = 10)
	private BigDecimal marketPriceUsd = new BigDecimal(1);

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	public Tradeable(String symbol, TradeableType type, String name, String description, BigDecimal marketPriceUsd) {
		this.symbol = symbol;
		this.type = type;
		this.name = name;
		this.description = description;
		this.marketPriceUsd = marketPriceUsd;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tradeable c)) return false;
		if (this.getId() != null && c.getId() != null) return this.getId().equals(c.getId());
		return this.getSymbol().equals(c.getSymbol()) && this.getType().equals(c.getType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.symbol);
	}
}
