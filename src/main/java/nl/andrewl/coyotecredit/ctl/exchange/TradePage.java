package nl.andrewl.coyotecredit.ctl.exchange;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.exchange.dto.TradePayload;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.ExchangeService;
import nl.andrewl.coyotecredit.service.TradeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/trade/{accountId}")
@RequiredArgsConstructor
public class TradePage {
	private final TradeService tradeService;
	private final ExchangeService exchangeService;

	@GetMapping
	public String get(
			Model model,
			@PathVariable long accountId,
			@AuthenticationPrincipal User user
	) {
		model.addAttribute("data", tradeService.getTradeData(accountId, user));
		return "account/trade";
	}

	@PostMapping
	public String doTrade(
			@PathVariable long accountId,
			@AuthenticationPrincipal User user,
			@ModelAttribute @Valid TradePayload payload
	) {
		exchangeService.doTrade(accountId, payload, user);
		return "redirect:/accounts/" + accountId;
	}
}
