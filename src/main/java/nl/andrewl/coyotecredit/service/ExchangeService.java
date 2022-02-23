package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.*;
import nl.andrewl.coyotecredit.dao.*;
import nl.andrewl.coyotecredit.model.*;
import nl.andrewl.coyotecredit.util.AccountNumberUtils;
import nl.andrewl.coyotecredit.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExchangeService {
	private final ExchangeRepository exchangeRepository;
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final TradeableRepository tradeableRepository;
	private final AccountValueSnapshotRepository accountValueSnapshotRepository;
	private final UserRepository userRepository;
	private final ExchangeInvitationRepository invitationRepository;
	private final JavaMailSender mailSender;

	@Value("${coyote-credit.base-url}")
	private String baseUrl;

	@Transactional(readOnly = true)
	public FullExchangeData getData(long exchangeId, User user) {
		Exchange e = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account userAccount = accountRepository.findByUserAndExchange(user, e)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		BigDecimal totalValue = BigDecimal.ZERO;
		int accountCount = 0;
		for (var acc : accountRepository.findAllByExchange(e)) {
			totalValue = totalValue.add(acc.getTotalBalance());
			accountCount++;
		}
		return new FullExchangeData(
				e.getId(),
				e.getName(),
				e.getDescription(),
				e.isPubliclyAccessible(),
				new TradeableData(e.getPrimaryTradeable()),
				e.getPrimaryBackgroundColor(),
				e.getSecondaryBackgroundColor(),
				e.getPrimaryForegroundColor(),
				e.getSecondaryForegroundColor(),
				e.getAllTradeables().stream()
						.map(TradeableData::new)
						.sorted(Comparator.comparing(TradeableData::symbol))
						.toList(),
				TradeableData.DECIMAL_FORMAT.format(totalValue),
				accountCount,
				userAccount.isAdmin(),
				userAccount.getId()
		);
	}

	@Transactional(readOnly = true)
	public List<SimpleAccountData> getAccounts(long exchangeId, User user) {
		Exchange exchange = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findByUserAndExchange(user, exchange)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		DecimalFormat df = new DecimalFormat("#,###0.00");
		return exchange.getAccounts().stream()
				.sorted(Comparator.comparing(Account::getName))
				.map(a -> new SimpleAccountData(
						a.getId(),
						a.getUser().getId(),
						a.getNumber(),
						a.getName(),
						a.isAdmin(),
						df.format(a.getTotalBalance()) + ' ' + exchange.getPrimaryTradeable().getSymbol()
				))
				.toList();
	}

	@Transactional(readOnly = true)
	public void ensureAdminAccount(long exchangeId, User user) {
		getExchangeIfAdmin(exchangeId, user);
	}

	private Exchange getExchangeIfAdmin(long exchangeId, User user) {
		Exchange exchange = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findByUserAndExchange(user, exchange)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return exchange;
	}

	@Transactional
	public void removeAccount(long exchangeId, long accountId, User user) {
		Exchange exchange = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account userAccount = accountRepository.findByUserAndExchange(user, exchange)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!userAccount.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		accountValueSnapshotRepository.deleteAllByAccount(account);
		accountRepository.delete(account);
	}

	@Transactional(readOnly = true)
	public Map<Long, String> getCurrentTradeables(long exchangeId) {
		Exchange e = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Map<Long, String> tradeables = new HashMap<>();
		for (var t : e.getAllTradeables()) {
			tradeables.put(t.getId(), t.getMarketPriceUsd().toPlainString());
		}
		return tradeables;
	}

	@Transactional
	public void doTrade(long accountId, TradePayload payload, User user) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found."));
		if (!account.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		Exchange exchange = account.getExchange();
		Tradeable from = tradeableRepository.findById(payload.sellTradeableId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sell tradeable not found."));
		Tradeable to = tradeableRepository.findById(payload.buyTradeableId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buy tradeable not found."));
		BigDecimal value = new BigDecimal(payload.value());
		if (from.getType().equals(TradeableType.STOCK)) {
			if (!payload.type().equalsIgnoreCase("SELL")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only perform SELL operations when selling stocks.");
			}
			if (to.getType().equals(TradeableType.STOCK)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot sell stock to purchase stock.");
			}
		}
		if (to.getType().equals(TradeableType.STOCK)) {
			if (!payload.type().equalsIgnoreCase("BUY")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only perform BUY operations when buying stocks.");
			}
			if (from.getType().equals(TradeableType.STOCK)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot sell stock to purchase stock.");
			}
		}
		if (value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only positive value may be specified.");
		}

		BigDecimal fromValue;
		BigDecimal toValue;
		if (payload.type().equalsIgnoreCase("SELL")) {
			fromValue = value;
			toValue = fromValue.multiply(from.getMarketPriceUsd()).divide(to.getMarketPriceUsd(), RoundingMode.HALF_UP);
		} else {
			toValue = value;
			fromValue = toValue.multiply(to.getMarketPriceUsd()).divide(from.getMarketPriceUsd(), RoundingMode.HALF_UP);
		}
		Balance fromBalance = account.getBalanceForTradeable(from);
		if (fromBalance == null || fromBalance.getAmount().compareTo(fromValue) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required balance of " + fromValue.toPlainString() + " from " + from.getSymbol());
		}
		fromBalance.setAmount(fromBalance.getAmount().subtract(fromValue));
		Balance toBalance = account.getBalanceForTradeable(to);
		if (toBalance == null) {
			toBalance = new Balance(account, to, toValue);
			account.getBalances().add(toBalance);
		} else {
			toBalance.setAmount(toBalance.getAmount().add(toValue));
		}
		accountRepository.save(account);
		Transaction tx = new Transaction(account.getNumber(), exchange, from, fromValue, to, toValue, LocalDateTime.now(ZoneOffset.UTC));
		transactionRepository.save(tx);
	}

	@Transactional(readOnly = true)
	public List<ExchangeAccountData> getExchanges(User user) {
		return accountRepository.findAllByUser(user).stream()
				.map(a -> new ExchangeAccountData(
						new ExchangeData(
								a.getExchange().getId(),
								a.getExchange().getName(),
								a.getExchange().getDescription(),
								a.getExchange().getPrimaryTradeable().getSymbol(),
								a.getExchange().getPrimaryTradeable().getId()
						),
						new SimpleAccountData(
								a.getId(),
								user.getId(),
								a.getNumber(),
								a.getName(),
								a.isAdmin(),
								TradeableData.DECIMAL_FORMAT.format(a.getTotalBalance())
						)
				))
				.sorted(Comparator.comparing(d -> d.exchange().name()))
				.toList();
	}

	@Transactional
	public void edit(long exchangeId, EditExchangePayload payload, User user) {
		Exchange e = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findByUserAndExchange(user, e)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		e.setName(payload.name());
		e.setDescription(payload.description());
		Tradeable primaryTradeable = tradeableRepository.findById(payload.primaryTradeableId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown primary tradeable currency."));
		if (!e.getAllTradeables().contains(primaryTradeable)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This exchange doesn't support " + primaryTradeable.getSymbol() + ".");
		}
		if (primaryTradeable.getType().equals(TradeableType.STOCK)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid primary tradeable currency. Stocks are not permitted.");
		}
		e.setPrimaryTradeable(primaryTradeable);
		e.setPubliclyAccessible(payload.publiclyAccessible());

		e.setPrimaryBackgroundColor(payload.primaryBackgroundColor());
		e.setSecondaryBackgroundColor(payload.secondaryBackgroundColor());
		e.setPrimaryForegroundColor(payload.primaryForegroundColor());
		e.setSecondaryForegroundColor(payload.secondaryForegroundColor());
		exchangeRepository.save(e);
	}

	@Transactional
	public void inviteUser(long exchangeId, User user, InviteUserPayload payload) {
		Exchange exchange = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findByUserAndExchange(user, exchange)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusDays(7);
		ExchangeInvitation invitation = invitationRepository.save(
				new ExchangeInvitation(exchange, account, payload.email(), StringUtils.random(64), expiresAt));
		Optional<User> invitedUser = userRepository.findByEmail(payload.email());
		if (invitedUser.isEmpty()) {
			try {
				sendInvitationEmail(invitation);
			} catch (MessagingException e) {
				e.printStackTrace();
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not send invitation email.");
			}
		}
	}

	@Transactional
	public void acceptInvite(long exchangeId, long inviteId, User user) {
		Exchange exchange = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		ExchangeInvitation invite = invitationRepository.findById(inviteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!invite.getUserEmail().equalsIgnoreCase(user.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite is for someone else.");
		}
		if (!invite.getExchange().getId().equals(exchangeId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite is for a different exchange.");
		}
		// If the user already has an account, silently delete the invite.
		if (accountRepository.existsByUserAndExchange(user, exchange)) {
			invitationRepository.delete(invite);
		}
		if (invite.isExpired()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite is expired.");
		}
		// Create the account.
		Account account = accountRepository.save(new Account(AccountNumberUtils.generate(), user, user.getUsername(), exchange));
		invitationRepository.delete(invite);
		for (var t : exchange.getAllTradeables()) {
			account.getBalances().add(new Balance(account, t, BigDecimal.ZERO));
		}
		accountRepository.save(account);
	}

	@Transactional
	public void rejectInvite(long exchangeId, long inviteId, User user) {
		ExchangeInvitation invite = invitationRepository.findById(inviteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!invite.getUserEmail().equalsIgnoreCase(user.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite is for someone else.");
		}
		if (!invite.getExchange().getId().equals(exchangeId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invite is for a different exchange.");
		}
		invitationRepository.delete(invite);
	}

	private void sendInvitationEmail(ExchangeInvitation invitation) throws MessagingException {
		MimeMessage msg = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg);
		helper.setFrom("Coyote Credit <noreply@coyote-credit.com>");
		helper.setTo(invitation.getUserEmail());
		helper.setSubject("Exchange Invitation");
		String url = baseUrl + "/register?inviteCode=" + invitation.getCode();
		helper.setText(String.format(
				"""
				<p>You have been invited by %s to join %s on Coyote Credit.
				Click the link below to register an account.</p>
				<a href="%s">%s</a>
				""",
				invitation.getSender().getName(),
				invitation.getExchange().getName(),
				url, url
		), true);
		mailSender.send(msg);
	}

	@Transactional(readOnly = true)
	public EditTradeablesData getEditTradeablesData(long exchangeId, User user) {
		Exchange exchange = getExchangeIfAdmin(exchangeId, user);
		List<TradeableData> supportedPublicTradeables = exchange.getSupportedTradeables().stream()
				.map(TradeableData::new).sorted(Comparator.comparing(TradeableData::symbol)).toList();
		List<TradeableData> customTradeables = exchange.getCustomTradeables().stream()
				.map(TradeableData::new).sorted(Comparator.comparing(TradeableData::symbol)).toList();
		List<TradeableData> eligiblePublicTradeables = tradeableRepository.findAllByExchangeNull().stream()
				.filter(t -> !exchange.getSupportedTradeables().contains(t))
				.map(TradeableData::new).sorted(Comparator.comparing(TradeableData::symbol)).toList();
		return new EditTradeablesData(
				supportedPublicTradeables,
				customTradeables,
				new TradeableData(exchange.getPrimaryTradeable()),
				eligiblePublicTradeables
		);
	}

	@Transactional
	public void addSupportedTradeable(long exchangeId, long tradeableId, User user) {
		Exchange exchange = getExchangeIfAdmin(exchangeId, user);
		Tradeable tradeable = tradeableRepository.findById(tradeableId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown tradeable."));
		// Exit if the tradeable is already supported.
		if (exchange.getSupportedTradeables().contains(tradeable)) return;
		if (tradeable.getExchange() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown tradeable.");
		}
		exchange.getSupportedTradeables().add(tradeable);
		// Add a zero-value balance to any account that's missing it.
		for (var acc : exchange.getAccounts()) {
			Balance bal = acc.getBalanceForTradeable(tradeable);
			if (bal == null) {
				acc.getBalances().add(new Balance(acc, tradeable, BigDecimal.ZERO));
				accountRepository.save(acc);
			}
		}
		exchangeRepository.save(exchange);
	}

	@Transactional
	public void removeSupportedTradeable(long exchangeId, long tradeableId, User user) {
		Exchange exchange = getExchangeIfAdmin(exchangeId, user);
		Tradeable tradeable = tradeableRepository.findById(tradeableId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown tradeable."));
		// Quietly exit if the user is trying to remove a tradeable that isn't supported in the first place.
		if (!exchange.getSupportedTradeables().contains(tradeable)) return;
		if (exchange.getPrimaryTradeable().equals(tradeable)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the primary tradeable asset. Change this first.");
		}
		exchange.getSupportedTradeables().remove(tradeable);
		// Delete balance for any account that has it.
		for (var acc : exchange.getAccounts()) {
			Balance bal = acc.getBalanceForTradeable(tradeable);
			if (bal != null) {
				acc.getBalances().remove(bal);
				accountRepository.save(acc);
			}
		}
		exchangeRepository.save(exchange);
	}
}
