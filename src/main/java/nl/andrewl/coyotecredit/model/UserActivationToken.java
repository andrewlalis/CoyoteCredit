package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserActivationToken {
	@Id
	@Column(nullable = false, unique = true, updatable = false)
	private String token;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private User user;

	/**
	 * The time at which this token expires, in UTC.
	 */
	@Column(nullable = false)
	private LocalDateTime expiresAt;

	public UserActivationToken(String token, User user, LocalDateTime expiresAt) {
		this.token = token;
		this.user = user;
		this.expiresAt = expiresAt;
	}
}
