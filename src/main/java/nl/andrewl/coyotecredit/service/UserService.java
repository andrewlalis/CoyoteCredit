package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.UserData;
import nl.andrewl.coyotecredit.dao.UserRepository;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	@Transactional
	public UserData getUser(long userId, User requestingUser) {
		User user;
		if (requestingUser.getId().equals(userId)) {
			user = requestingUser;
		} else {
			user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		}
		return new UserData(user.getId(), user.getUsername());
	}
}
