package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.service.TradeableService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/tradeables/{tradeableId}")
@RequiredArgsConstructor
public class TradeablePage {
	private final TradeableService tradeableService;

	@GetMapping
	public String get(Model model, @PathVariable long tradeableId, @AuthenticationPrincipal User user) {
		model.addAttribute("tradeable", tradeableService.getTradeable(tradeableId, user));
		return "tradeable";
	}
}
