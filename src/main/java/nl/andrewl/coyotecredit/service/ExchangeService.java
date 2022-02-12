package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.*;
import nl.andrewl.coyotecredit.dao.*;
import nl.andrewl.coyotecredit.model.*;
import nl.andrewl.coyotecredit.util.AccountNumberUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeService {
	private final ExchangeRepository exchangeRepository;
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final TradeableRepository tradeableRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public FullExchangeData getData(long exchangeId, User user) {
		Exchange e = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findByUserAndExchange(user, e)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new FullExchangeData(
				e.getId(),
				e.getName(),
				new TradeableData(e.getPrimaryTradeable()),
				e.getAllTradeables().stream()
						.map(TradeableData::new)
						.sorted(Comparator.comparing(TradeableData::symbol))
						.toList(),
				account.isAdmin()
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
						a.getNumber(),
						a.getName(),
						a.isAdmin(),
						df.format(a.getTotalBalance()) + ' ' + exchange.getPrimaryTradeable().getSymbol()
				))
				.toList();
	}

	@Transactional(readOnly = true)
	public void ensureAdminAccount(long exchangeId, User user) {
		Exchange exchange = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findByUserAndExchange(user, exchange)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}

	@Transactional
	public long addAccount(long exchangeId, User user, AddAccountPayload payload) {
		Exchange exchange = exchangeRepository.findById(exchangeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account account = accountRepository.findByUserAndExchange(user, exchange)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		User u = userRepository.save(new User(payload.username(), passwordEncoder.encode(payload.password())));
		Account a = accountRepository.save(new Account(
				AccountNumberUtils.generate(),
				u,
				payload.name(),
				exchange
		));
		for (var t : exchange.getAllTradeables()) {
			a.getBalances().add(new Balance(a, t, BigDecimal.ZERO));
		}
		a = accountRepository.save(a);
		return a.getId();
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
}
