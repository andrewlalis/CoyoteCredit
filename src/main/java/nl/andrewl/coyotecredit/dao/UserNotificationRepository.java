package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
	@Query("SELECT un FROM UserNotification un " +
			"WHERE un.user = :user AND un.dismissed = FALSE " +
			"ORDER BY un.sentAt DESC")
	List<UserNotification> findAllNewNotifications(User user);

	@Query("SELECT COUNT(un) FROM UserNotification un " +
			"WHERE un.user = :user AND un.dismissed = FALSE")
	long countAllNewNotifications(User user);
}
