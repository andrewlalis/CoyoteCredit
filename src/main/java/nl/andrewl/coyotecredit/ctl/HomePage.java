package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class HomePage {
	private final AccountService accountService;

	@GetMapping
	public String get(Model model, @AuthenticationPrincipal User user) {
		model.addAttribute("accounts", accountService.getAccountsOverview(user));
		return "home";
	}
}
