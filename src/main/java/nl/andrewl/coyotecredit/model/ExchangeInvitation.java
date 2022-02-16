package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents an invitation which an admin can send to users (or any email
 * address) to invite them to participate in an exchange. If no user with the
 * given email address exists, then a registration code will be sent to the
 * email address, which when present at the /register page, will give context
 * to allow the user to be automatically added to the exchange.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ExchangeInvitation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exchange exchange;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Account sender;

	@Column(nullable = false)
	private String userEmail;

	@Column(nullable = false, unique = true)
	private String code;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	public ExchangeInvitation(Exchange exchange, Account sender, String userEmail, String code, LocalDateTime expiresAt) {
		this.exchange = exchange;
		this.sender = sender;
		this.userEmail = userEmail;
		this.code = code;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired() {
		return getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC));
	}
}
