package nl.andrewl.coyotecredit.ctl;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.RegisterPayload;
import nl.andrewl.coyotecredit.dao.ExchangeInvitationRepository;
import nl.andrewl.coyotecredit.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PublicPageController {
	private final UserService userService;
	private final ExchangeInvitationRepository invitationRepository;

	@GetMapping(path = "/login")
	public String getLoginPage() {
		return "public/login";
	}

	@GetMapping(path = "/register") @Transactional(readOnly = true)
	public String getRegisterPage(
			Model model,
			@RequestParam(name = "inviteCode", required = false) String inviteCode
	) {
		if (inviteCode != null && !inviteCode.isBlank()) {
			invitationRepository.findByCode(inviteCode).ifPresent(invite -> {
				model.addAttribute("inviteCode", inviteCode);
				model.addAttribute("inviteUserEmail", invite.getUserEmail());
				model.addAttribute("inviteSenderName", invite.getSender().getName());
				model.addAttribute("inviteExchangeName", invite.getExchange().getName());
			});
		}
		return "public/register";
	}

	@PostMapping(path = "/register")
	public String postRegister(@ModelAttribute RegisterPayload payload) {
		userService.registerUser(payload);
		return "redirect:/login";
	}

	@GetMapping(path = "/activate")
	public String activateAccount(@RequestParam(name = "token") String token) {
		userService.activateUser(token);
		return "redirect:/login";
	}
}
