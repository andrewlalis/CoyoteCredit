package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a transfer of funds from one account to another.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Transfer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, updatable = false)
	private LocalDateTime timestamp;

	@Column(nullable = false)
	private String senderNumber;

	@Column(nullable = false)
	private String recipientNumber;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private Tradeable tradeable;

	@Column(nullable = false, precision = 24, scale = 10)
	private BigDecimal amount;

	@Column(length = 1024)
	private String message;

	public Transfer(String sender, String recipient, Tradeable tradeable, BigDecimal amount, String message) {
		this.senderNumber = sender;
		this.recipientNumber = recipient;
		this.tradeable = tradeable;
		this.amount = amount;
		this.message = message;
		this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
	}
}
