package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.RequestExchangePayload;
import nl.andrewl.coyotecredit.dao.ExchangeRequestRepository;
import nl.andrewl.coyotecredit.model.ExchangeRequest;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/requestExchange")
@RequiredArgsConstructor
public class RequestExchangeController {
	private final ExchangeRequestRepository exchangeRequestRepository;

	@GetMapping
	public String get() {
		return "exchange/request_exchange";
	}

	@PostMapping
	@Transactional
	public String post(@AuthenticationPrincipal User user, @ModelAttribute RequestExchangePayload payload) {
		exchangeRequestRepository.save(new ExchangeRequest(
				user, payload.estimatedAccountCount(), payload.organization(), payload.reason()
		));
		return "redirect:/";
	}
}
