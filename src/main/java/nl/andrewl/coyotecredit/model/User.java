package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a basic user in the system.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false) @Setter
	private String passwordHash;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false) @Setter
	private boolean activated = false;

	@Column(nullable = false) @Setter
	private boolean admin = false;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * The set of accounts this user has.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Account> accounts;

	@Setter
	private transient long newNotificationCount;
	@Setter
	private transient boolean hasNotifications;

	public User(String username, String passwordHash, String email) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.email = email;
		this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
		this.accounts = new HashSet<>();
	}

	// USER DETAILS IMPLEMENTATION

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}

	@Override
	public String getPassword() {
		return this.passwordHash;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.activated;
	}
}
