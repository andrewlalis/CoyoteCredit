package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.AddAccountPayload;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.ExchangeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/exchanges/{exchangeId}")
@RequiredArgsConstructor
public class ExchangePage {
	private final ExchangeService exchangeService;

	@GetMapping
	public String get(Model model, @PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		model.addAttribute("exchange", exchangeService.getData(exchangeId, user));
		return "exchange";
	}

	@GetMapping(path = "/accounts")
	public String getAccountsPage(Model model, @PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		model.addAttribute("accounts", exchangeService.getAccounts(exchangeId, user));
		return "exchange/accounts";
	}

	@GetMapping(path = "/addAccount")
	public String getAddAccountPage(@PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		exchangeService.ensureAdminAccount(exchangeId, user);
		return "exchange/addAccount";
	}

	@PostMapping(path = "/addAccount")
	public String postAddAcount(@PathVariable long exchangeId, @AuthenticationPrincipal User user, @ModelAttribute AddAccountPayload payload) {
		long accountId = exchangeService.addAccount(exchangeId, user, payload);
		return "redirect:/accounts/" + accountId;
	}

	@GetMapping(path = "/removeAccount/{accountId}")
	public String getRemoveAccountPage(@PathVariable long exchangeId, @PathVariable long accountId, @AuthenticationPrincipal User user) {
		exchangeService.ensureAdminAccount(exchangeId, user);
		return "exchange/removeAccount";
	}

	@PostMapping(path = "/removeAccount/{accountId}")
	public String postRemoveAccount(@PathVariable long exchangeId, @PathVariable long accountId, @AuthenticationPrincipal User user) {
		exchangeService.removeAccount(exchangeId, accountId, user);
		return "redirect:/exchanges/" + exchangeId + "/accounts";
	}
}
