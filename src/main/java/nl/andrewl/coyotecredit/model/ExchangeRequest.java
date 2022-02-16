package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Represents a user's request to create a new exchange.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ExchangeRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false)
	private int estimatedAccountCount;

	@Column
	private String organization;

	@Column(nullable = false)
	private String reason;

	public ExchangeRequest(User user, int estimatedAccountCount, String organization, String reason) {
		this.user = user;
		this.estimatedAccountCount = estimatedAccountCount;
		this.organization = organization;
		this.reason = reason;
	}
}
