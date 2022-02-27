package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.AddTradeablePayload;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.TradeableService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class AdminController {
	private final TradeableService tradeableService;

	@GetMapping
	public String get(@AuthenticationPrincipal User user) {
		if (!user.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return "admin/admin";
	}

	@GetMapping(path = "/manageTradeables")
	public String getManageTradeablesPage(Model model, @AuthenticationPrincipal User user) {
		if (!user.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		model.addAttribute("tradeables", tradeableService.getAllPublicTradeables(user));
		return "admin/manage_tradeables";
	}

	@GetMapping(path = "/manageTradeables/add")
	public String getAddTradeablePage(@AuthenticationPrincipal User user) {
		if (!user.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return "admin/add_tradeable";
	}

	@PostMapping(path = "/manageTradeables/add")
	public String addTradeable(@AuthenticationPrincipal User user, @ModelAttribute AddTradeablePayload payload) {
		tradeableService.addTradeable(user, payload);
		return "redirect:/admin/manageTradeables";
	}

	@GetMapping(path = "/manageTradeables/remove/{tradeableId}")
	public String getRemoveTradeablePage(@AuthenticationPrincipal User user, @PathVariable long tradeableId) {
		tradeableService.checkRemovePage(user, tradeableId);
		return "admin/remove_tradeable";
	}

	@PostMapping(path = "/manageTradeables/remove/{tradeableId}")
	public String removeTradeable(@AuthenticationPrincipal User user, @PathVariable long tradeableId) {
		tradeableService.removeTradeable(user, tradeableId);
		return "redirect:/admin/manageTradeables";
	}
}
