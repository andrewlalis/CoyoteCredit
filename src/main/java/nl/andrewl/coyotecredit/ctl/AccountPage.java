package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.TransferPayload;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

	@GetMapping(path = "/editBalances")
	public String getEditBalancesPage(Model model, @PathVariable long accountId, @AuthenticationPrincipal User user) {
		var data = accountService.getAccountData(user, accountId);
		if (!data.userAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		model.addAttribute("account", data);
		return "account/edit_balances";
	}

	@PostMapping(path = "/editBalances")
	public String postEditBalances(@PathVariable long accountId, @AuthenticationPrincipal User user, @RequestParam MultiValueMap<String, String> paramMap) {
		accountService.editBalances(accountId, user, paramMap);
		return "redirect:/accounts/" + accountId;
	}

	@GetMapping(path = "/transfer")
	public String getTransferPage(Model model, @PathVariable long accountId, @AuthenticationPrincipal User user) {
		model.addAttribute("balances", accountService.getTransferData(accountId, user));
		return "account/transfer";
	}

	@PostMapping(path = "/transfer")
	public String postTransfer(@PathVariable long accountId, @ModelAttribute TransferPayload payload, @AuthenticationPrincipal User user) {
		accountService.transfer(accountId, user, payload);
		return "redirect:/accounts/" + accountId;
	}
}
