package nl.andrewl.coyotecredit.ctl.user;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/userNotifications")
@RequiredArgsConstructor
public class NotificationController {
	private final UserService userService;

	@PostMapping(path = "/{notificationId}/dismiss")
	public String dismiss(@PathVariable long notificationId, @AuthenticationPrincipal User user) {
		userService.dismissNotification(user, notificationId);
		return "redirect:/users/" + user.getId();
	}

	@PostMapping(path = "/dismissAll")
	public String dismissAll(@AuthenticationPrincipal User user) {
		userService.dismissAllNotifications(user);
		return "redirect:/users/" + user.getId();
	}
}
