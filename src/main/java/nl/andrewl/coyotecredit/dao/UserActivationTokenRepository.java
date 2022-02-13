package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.UserActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserActivationTokenRepository extends JpaRepository<UserActivationToken, String> {
	void deleteAllByExpiresAtBefore(LocalDateTime time);
}
