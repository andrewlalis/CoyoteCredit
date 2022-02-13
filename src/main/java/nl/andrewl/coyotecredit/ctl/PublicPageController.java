package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.RegisterPayload;
import nl.andrewl.coyotecredit.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PublicPageController {
	private final UserService userService;

	@GetMapping(path = "/login")
	public String getLoginPage() {
		return "public/login";
	}

	@GetMapping(path = "/register")
	public String getRegisterPage() {
		return "public/register";
	}

	@PostMapping(path = "/register")
	public String postRegister(@ModelAttribute RegisterPayload payload) {
		userService.registerUser(payload);
		return "redirect:/login";
	}

	@GetMapping(path = "/activate")
	public String activateAccount(@RequestParam(name = "token") String token) {
		userService.activateUser(token);
		return "redirect:/login";
	}
}
