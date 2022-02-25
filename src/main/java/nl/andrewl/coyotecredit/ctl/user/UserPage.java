package nl.andrewl.coyotecredit.ctl.user;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
