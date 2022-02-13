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
@RequestMapping(path = "/exchanges")
@RequiredArgsConstructor
public class ExchangeController {
	private final ExchangeService exchangeService;

	@GetMapping
	public String getExchanges(Model model, @AuthenticationPrincipal User user) {
		model.addAttribute("exchangeData", exchangeService.getExchanges(user));
		return "exchange/exchanges";
	}

	@GetMapping(path = "/{exchangeId}")
	public String get(Model model, @PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		model.addAttribute("exchange", exchangeService.getData(exchangeId, user));
		return "exchange/exchange";
	}

	@GetMapping(path = "/{exchangeId}/accounts")
	public String getAccountsPage(Model model, @PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		model.addAttribute("accounts", exchangeService.getAccounts(exchangeId, user));
		return "exchange/accounts";
	}

	@GetMapping(path = "/{exchangeId}/addAccount")
	public String getAddAccountPage(@PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		exchangeService.ensureAdminAccount(exchangeId, user);
		return "exchange/addAccount";
	}

	@PostMapping(path = "/{exchangeId}/addAccount")
	public String postAddAcount(@PathVariable long exchangeId, @AuthenticationPrincipal User user, @ModelAttribute AddAccountPayload payload) {
		long accountId = exchangeService.addAccount(exchangeId, user, payload);
		return "redirect:/accounts/" + accountId;
	}

	@GetMapping(path = "/{exchangeId}/removeAccount/{accountId}")
	public String getRemoveAccountPage(@PathVariable long exchangeId, @PathVariable long accountId, @AuthenticationPrincipal User user) {
		exchangeService.ensureAdminAccount(exchangeId, user);
		return "exchange/removeAccount";
	}

	@PostMapping(path = "/{exchangeId}/removeAccount/{accountId}")
	public String postRemoveAccount(@PathVariable long exchangeId, @PathVariable long accountId, @AuthenticationPrincipal User user) {
		exchangeService.removeAccount(exchangeId, accountId, user);
		return "redirect:/exchanges/" + exchangeId + "/accounts";
	}
}
