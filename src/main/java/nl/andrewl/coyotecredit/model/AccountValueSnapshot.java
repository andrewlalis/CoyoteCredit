package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a snapshot in time of an account's total value.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AccountValueSnapshot {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Account account;

	@Column(nullable = false, precision = 24, scale = 10)
	private BigDecimal amount;

	@Column(nullable = false, updatable = false)
	private LocalDateTime timestamp;

	public AccountValueSnapshot(Account account, LocalDateTime timestamp, BigDecimal amount) {
		this.account = account;
		this.timestamp = timestamp;
		this.amount = amount;
	}
}
