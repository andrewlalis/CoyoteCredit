package nl.andrewl.coyotecredit.ctl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/login")
public class LoginPage {
	@GetMapping
	public String get() {
		return "login";
	}
}
