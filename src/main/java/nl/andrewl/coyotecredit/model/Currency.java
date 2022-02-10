package nl.andrewl.coyotecredit.model;

import lombok.Getter;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents a type of currency. This can be an actual fiat currency, or
 * perhaps a cryptocurrency, or stocks, or really anything tradeable on the
 * exchange.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"identifier", "type"}))
@Getter
public class Currency {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String identifier;

	@Enumerated(EnumType.STRING)
	private CurrencyType type;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	@Column(nullable = false)
	private float minDenomination = 0.01f;

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Currency c)) return false;
		if (c.getId() != null && this.getId() != null) return this.getId().equals(c.getId());
		return this.identifier.equals(c.getIdentifier()) &&
				this.type.equals(c.getType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.identifier, this.type);
	}
}
