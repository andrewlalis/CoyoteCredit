package nl.andrewl.coyotecredit.model;

import lombok.Getter;

import javax.persistence.*;

/**
 * Represents a type of currency. This can be an actual fiat currency, or
 * perhaps a cryptocurrency, or stocks, or really anything tradeable on the
 * exchange.
 */
@Entity
@Getter
public class Currency {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String identifier;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;
}
