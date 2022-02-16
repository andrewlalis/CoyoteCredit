package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.EditExchangePayload;
import nl.andrewl.coyotecredit.ctl.dto.InviteUserPayload;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.ExchangeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

	@GetMapping(path = "/{exchangeId}/inviteUser")
	public String getInviteUserPage(@PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		exchangeService.ensureAdminAccount(exchangeId, user);
		return "exchange/invite_user";
	}

	@PostMapping(path = "/{exchangeId}/inviteUser")
	public String postInviteUser(@PathVariable long exchangeId, @AuthenticationPrincipal User user, @ModelAttribute InviteUserPayload payload) {
		exchangeService.inviteUser(exchangeId, user, payload);
		return "redirect:/exchanges/" + exchangeId;
	}

	@PostMapping(path = "/{exchangeId}/acceptInvite/{inviteId}")
	public String postAcceptInvite(@PathVariable long exchangeId, @PathVariable long inviteId, @AuthenticationPrincipal User user) {
		exchangeService.acceptInvite(exchangeId, inviteId, user);
		return "redirect:/exchanges/" + exchangeId;
	}

	@PostMapping(path = "/{exchangeId}/rejectInvite/{inviteId}")
	public String postRejectInvite(@PathVariable long exchangeId, @PathVariable long inviteId, @AuthenticationPrincipal User user) {
		exchangeService.rejectInvite(exchangeId, inviteId, user);
		return "redirect:/users/" + user.getId();
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

	@GetMapping(path = "/{exchangeId}/edit")
	public String getEditPage(Model model, @PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		model.addAttribute("exchange", exchangeService.getData(exchangeId, user));
		return "exchange/edit";
	}

	@PostMapping(path = "/{exchangeId}/edit")
	public String postEdit(@PathVariable long exchangeId, @AuthenticationPrincipal User user, @Valid @ModelAttribute EditExchangePayload payload) {
		exchangeService.edit(exchangeId, payload, user);
		return "redirect:/exchanges/" + exchangeId;
	}
}
