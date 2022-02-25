package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a notification that is shown to a user, and can be dismissed once
 * the user has read it.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserNotification {
	public static final int MAX_LENGTH = 2048;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false, updatable = false)
	private LocalDateTime sentAt;

	@Column(nullable = false, updatable = false, length = MAX_LENGTH)
	private String content;

	@Column(nullable = false) @Setter
	private boolean dismissed = false;

	@Column @Setter
	private LocalDateTime dismissedAt;

	public UserNotification(User user, String content) {
		this.user = user;
		this.content = content;
		this.sentAt = LocalDateTime.now(ZoneOffset.UTC);
	}
}
