package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a permanent record of a trade transaction.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String accountNumber;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exchange exchange;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Tradeable from;

	@Column(nullable = false, precision = 24, scale = 10)
	private BigDecimal fromAmount;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Tradeable to;

	@Column(nullable = false, precision = 24, scale = 10)
	private BigDecimal toAmount;

	@Column(nullable = false, updatable = false)
	private LocalDateTime timestamp;

	public Transaction(String accountNumber, Exchange exchange, Tradeable from, BigDecimal fromAmount, Tradeable to, BigDecimal toAmount, LocalDateTime timestamp) {
		this.accountNumber = accountNumber;
		this.exchange = exchange;
		this.from = from;
		this.fromAmount = fromAmount;
		this.to = to;
		this.toAmount = toAmount;
		this.timestamp = timestamp;
	}
}
