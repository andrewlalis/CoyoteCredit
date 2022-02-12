package nl.andrewl.coyotecredit.ctl.api;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.service.ExchangeService;
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
	public Map<Long, String> getCurrentTradeables(@PathVariable long exchangeId) {
		return exchangeService.getCurrentTradeables(exchangeId);
	}
}
