package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/accounts/{accountId}")
@RequiredArgsConstructor
public class AccountPage {
	private final AccountService accountService;

	@GetMapping
	public String get(
			Model model,
			@PathVariable long accountId,
			@AuthenticationPrincipal User user
	) {
		var data = accountService.getAccountData(user, accountId);
		model.addAttribute("account", data);
		return "account";
	}
}
