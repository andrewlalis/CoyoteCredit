package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.exchange.dto.TradeData;
import nl.andrewl.coyotecredit.ctl.dto.TradeableData;
import nl.andrewl.coyotecredit.dao.AccountRepository;
import nl.andrewl.coyotecredit.model.Account;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TradeService {
	private final AccountRepository accountRepository;

	@Transactional(readOnly = true)
	public TradeData getTradeData(long accountId, User user) {
		Account account = accountRepository.findById(accountId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		List<TradeableData> sellList = new ArrayList<>();
		Map<String, BigDecimal> accountBalances = new HashMap<>();
		for (var bal : account.getBalances()) {
			sellList.add(new TradeableData(bal.getTradeable()));
			accountBalances.put(bal.getTradeable().getSymbol(), bal.getAmount());
		}
		sellList.sort(Comparator.comparing(TradeableData::symbol));
		List<TradeableData> buyList = new ArrayList<>();
		for (var t : account.getExchange().getAllTradeables()) {
			buyList.add(new TradeableData(t));
		}

		return new TradeData(account.getId(), account.getNumber(), account.getExchange().getId(), sellList, accountBalances, buyList);
	}
}
