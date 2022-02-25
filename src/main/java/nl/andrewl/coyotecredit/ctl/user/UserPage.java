package nl.andrewl.coyotecredit.ctl.user;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.user.dto.ChangePasswordPayload;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/users/{userId}")
@RequiredArgsConstructor
public class UserPage {
	private final UserService userService;

	@GetMapping
	public String get(Model model, @PathVariable long userId, @AuthenticationPrincipal User user) {
		model.addAttribute("user", userService.getUser(userId, user));
		return "user";
	}

	@GetMapping(path = "/changePassword")
	public String getChangePasswordPage(@PathVariable long userId, @AuthenticationPrincipal User user) {
		userService.ensureCanChangePassword(user, userId);
		return "user/change_password";
	}

	@PostMapping(path = "/changePassword")
	public String postChangePassword(@PathVariable long userId, @AuthenticationPrincipal User user, @ModelAttribute ChangePasswordPayload payload) {
		userService.changePassword(user, userId, payload);
		return "redirect:/users/" + userId;
	}
}
