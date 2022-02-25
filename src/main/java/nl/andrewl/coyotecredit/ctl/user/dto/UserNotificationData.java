package nl.andrewl.coyotecredit.ctl.user.dto;

import nl.andrewl.coyotecredit.model.UserNotification;

import java.time.format.DateTimeFormatter;

public record UserNotificationData(
		long id,
		String sentAt,
		String content,
		boolean dismissed
) {
	public UserNotificationData(UserNotification n) {
		this(n.getId(), n.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC", n.getContent(), n.isDismissed());
	}
}
