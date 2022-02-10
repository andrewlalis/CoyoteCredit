package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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


}
