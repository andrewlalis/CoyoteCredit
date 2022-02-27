package nl.andrewl.coyotecredit.ctl.api;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.exchange.dto.PublicAccountData;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.ExchangeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/exchanges/{exchangeId}")
@RequiredArgsConstructor
public class ExchangeApiController {
	private final ExchangeService exchangeService;

	@GetMapping(path = "/tradeables")
	public Map<Long, String> getCurrentTradeables(@PathVariable long exchangeId, @AuthenticationPrincipal User user) {
		return exchangeService.getCurrentTradeables(exchangeId, user);
	}

	@GetMapping(path = "/accounts/{number}")
	public PublicAccountData getAccountData(@PathVariable long exchangeId, @PathVariable String number, @AuthenticationPrincipal User user) {
		return exchangeService.getPublicAccountData(exchangeId, number, user);
	}
}
