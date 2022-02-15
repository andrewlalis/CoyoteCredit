package nl.andrewl.coyotecredit.ctl;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
@ControllerAdvice
@RequestMapping(path = "/error")
public class ErrorPage implements ErrorController {
	@GetMapping
	public String get(Model model, HttpServletRequest request) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		int statusCode = status == null ? -1 : (Integer) status;
		model.addAttribute("statusCode", statusCode);
		Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String msg;
		if (message != null) {
			msg = (String) message;
		} else {
			msg = switch (statusCode) {
				case 404 -> "Not found.";
				case 400 -> "Bad request.";
				case 401 -> "Unauthorized.";
				case 403 -> "Forbidden.";
				case 500 -> "Internal server error.";
				default -> "Unknown error.";
			};
		}
		model.addAttribute("message", msg);
		return "error";
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ModelAndView handleException(ResponseStatusException e) {
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("statusCode", e.getRawStatusCode());
		mav.addObject("message", e.getReason());
		return mav;
	}
}
