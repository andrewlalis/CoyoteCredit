package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.InvitationData;
import nl.andrewl.coyotecredit.ctl.dto.RegisterPayload;
import nl.andrewl.coyotecredit.ctl.dto.UserData;
import nl.andrewl.coyotecredit.dao.AccountRepository;
import nl.andrewl.coyotecredit.dao.ExchangeInvitationRepository;
import nl.andrewl.coyotecredit.dao.UserActivationTokenRepository;
import nl.andrewl.coyotecredit.dao.UserRepository;
import nl.andrewl.coyotecredit.model.Account;
import nl.andrewl.coyotecredit.model.Balance;
import nl.andrewl.coyotecredit.model.User;
import nl.andrewl.coyotecredit.model.UserActivationToken;
import nl.andrewl.coyotecredit.util.AccountNumberUtils;
import nl.andrewl.coyotecredit.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final UserActivationTokenRepository activationTokenRepository;
	private final ExchangeInvitationRepository exchangeInvitationRepository;
	private final JavaMailSender mailSender;
	private final PasswordEncoder passwordEncoder;

	@Value("${coyote-credit.base-url}")
	private String baseUrl;

	@Transactional(readOnly = true)
	public UserData getUser(long userId, User requestingUser) {
		User user;
		if (requestingUser.getId().equals(userId)) {
			user = requestingUser;
		} else {
			user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		}
		List<InvitationData> exchangeInvitations = new ArrayList<>();
		for (var invitation : exchangeInvitationRepository.findAllByUserEmail(user.getEmail())) {
			if (invitation.isExpired()) continue;
			exchangeInvitations.add(new InvitationData(invitation.getId(), invitation.getExchange().getId(), invitation.getExchange().getName()));
		}
		exchangeInvitations.sort(Comparator.comparing(InvitationData::id));
		return new UserData(user.getId(), user.getUsername(), user.getEmail(), exchangeInvitations);
	}

	@Transactional
	public void registerUser(RegisterPayload payload) {
		if (userRepository.existsByUsername(payload.username())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken.");
		}
		User user = new User(payload.username(), passwordEncoder.encode(payload.password()), payload.email());
		user = userRepository.save(user);
		if (payload.inviteCode() != null) {
			var invite = exchangeInvitationRepository.findByCode(payload.inviteCode())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation code."));
			if (!invite.getUserEmail().equalsIgnoreCase(user.getEmail())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invitation code is for somebody else.");
			}
			if (invite.isExpired()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invitation code is expired.");
			}
			exchangeInvitationRepository.delete(invite);
			Account account = new Account(AccountNumberUtils.generate(), user, payload.accountName(), invite.getExchange());
			account = accountRepository.save(account);
			for (var tradeable : invite.getExchange().getAllTradeables()) {
				account.getBalances().add(new Balance(account, tradeable, BigDecimal.ZERO));
			}
			account = accountRepository.save(account);
			user.getAccounts().add(account);
			user.setActivated(true);
			user = userRepository.save(user);
		} else {
			String token = StringUtils.random(64);
			LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusHours(24);
			UserActivationToken activationToken = new UserActivationToken(token, user, expiresAt);
			activationTokenRepository.save(activationToken);
			try {
				sendActivationEmail(activationToken);
			} catch (MessagingException e) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not send activation email.");
			}
		}
	}

	@Transactional
	public void activateUser(String tokenString) {
		UserActivationToken token = activationTokenRepository.findById(tokenString)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid activation code."));
		if (token.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activation code is expired.");
		}
		token.getUser().setActivated(true);
		activationTokenRepository.delete(token);
		userRepository.save(token.getUser());
	}

	private void sendActivationEmail(UserActivationToken token) throws MessagingException {
		MimeMessage msg = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg);
		helper.setFrom("Coyote Credit <noreply@coyote-credit.com>");
		helper.setTo(token.getUser().getEmail());
		helper.setSubject("Activate Your Account");
		String activationUrl = baseUrl + "/activate?token=" + token.getToken();
		helper.setText(String.format(
				"""
				<p>In order to complete your account registration for Coyote Credit,
				please follow this link:</p>
				<a href="%s">%s</a>.
				<p>Note that this link will expire in 24 hours.</p>
				<p>If you did not register for an account, or you are unaware of
				someone registering on your behalf, you may safely ignore this
				email.</p>""",
				activationUrl, activationUrl
		), true);
		mailSender.send(msg);
	}

	@Scheduled(cron = "@midnight")
	public void removeExpiredActivationTokens() {
		activationTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now(ZoneOffset.UTC));
	}
}
