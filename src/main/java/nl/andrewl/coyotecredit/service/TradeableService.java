package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.AddTradeablePayload;
import nl.andrewl.coyotecredit.ctl.dto.TradeableData;
import nl.andrewl.coyotecredit.dao.*;
import nl.andrewl.coyotecredit.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeableService {
	private final TradeableRepository tradeableRepository;
	private final AccountRepository accountRepository;
	private final ExchangeRepository exchangeRepository;
	private final TransactionRepository transactionRepository;
	private final TradeableUpdateService tradeableUpdateService;
	private final UserNotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public TradeableData getTradeable(long id, User user) {
		Tradeable tradeable = tradeableRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (tradeable.getExchange() != null && !accountRepository.existsByUserAndExchange(user, tradeable.getExchange())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return new TradeableData(tradeable);
	}

	@Transactional(readOnly = true)
	public List<TradeableData> getAllPublicTradeables(User user) {
		if (!user.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return tradeableRepository.findAllByExchangeNull()
				.stream().map(TradeableData::new)
				.toList();
	}

	@Transactional
	public void addTradeable(User user, AddTradeablePayload payload) {
		if (!user.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		if (tradeableRepository.existsByExchangeNullAndSymbol(payload.symbol())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tradeable with that symbol already exists.");
		}
		TradeableType type;
		try {
			type = TradeableType.valueOf(payload.type());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type.", e);
		}
		Tradeable tradeable = new Tradeable(payload.symbol(), type, payload.name(), payload.description(), BigDecimal.ZERO, null);
		tradeable = tradeableRepository.save(tradeable);
		tradeableUpdateService.updateTradeable(tradeable);
	}

	@Transactional(readOnly = true)
	public void checkRemovePage(User user, long tradeableId) {
		if (!user.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		Tradeable t = tradeableRepository.findById(tradeableId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (t.getExchange() != null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		if (t.getSymbol().equalsIgnoreCase("USD")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	public void removeTradeable(User user, long tradeableId) {
		if (!user.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		Tradeable t = tradeableRepository.findById(tradeableId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (t.getExchange() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove exchange-custom tradeable.");
		if (t.getSymbol().equalsIgnoreCase("USD")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove USD.");
		for (Account account : accountRepository.findAllWithBalanceForTradeable(t)) {
			Balance bal = account.getBalanceForTradeable(t);
			if (bal != null) {
				account.getBalances().remove(bal);
				notificationRepository.save(new UserNotification(
						account.getUser(),
						String.format("Your balance of %s %s in %s has been removed because Coyote Credit has removed support for it.",
								bal.getAmount().toPlainString(),
								t.getSymbol(),
								account.getExchange().getName())
				));
			}
		}
		transactionRepository.deleteAllWithTradeable(t);
		for (Exchange exchange : exchangeRepository.findAll()) {
			exchange.getSupportedTradeables().remove(t);
			exchangeRepository.save(exchange);
		}
		tradeableRepository.delete(t);
	}
}
