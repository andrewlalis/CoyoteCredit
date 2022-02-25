package nl.andrewl.coyotecredit.config;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.dao.UserNotificationRepository;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A filter that fetches and sets the user's notification count, which is
 * displayed in the header for easy checking.
 */
@Component
@RequiredArgsConstructor
public class UserNotificationSetFilter extends OncePerRequestFilter {
	private final UserNotificationRepository notificationRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			User user = (User) auth.getPrincipal();
			user.setNewNotificationCount(notificationRepository.countAllNewNotifications(user));
			user.setHasNotifications(user.getNewNotificationCount() > 0);
		}
		filterChain.doFilter(request, response);
	}
}
