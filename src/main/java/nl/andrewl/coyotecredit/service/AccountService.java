package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.BalanceData;
import nl.andrewl.coyotecredit.ctl.dto.ExchangeData;
import nl.andrewl.coyotecredit.ctl.dto.FullAccountData;
import nl.andrewl.coyotecredit.ctl.dto.TransactionData;
import nl.andrewl.coyotecredit.dao.AccountRepository;
import nl.andrewl.coyotecredit.dao.TradeableRepository;
import nl.andrewl.coyotecredit.dao.TransactionRepository;
import nl.andrewl.coyotecredit.model.Account;
import nl.andrewl.coyotecredit.model.Balance;
import nl.andrewl.coyotecredit.model.Tradeable;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final TradeableRepository tradeableRepository;

	public static record AccountData (
			long id,
			String accountNumber,
			String exchangeName
	) {}

	@Transactional(readOnly = true)
	public List<AccountData> getAccountsOverview(User user) {
		return accountRepository.findAllByUser(user).stream()
				.map(a -> new AccountData(
						a.getId(),
						a.getNumber(),
						a.getExchange().getName()
				))
				.toList();
	}

	@Transactional(readOnly = true)
	public FullAccountData getAccountData(User user, long accountId) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account userAccount = accountRepository.findByUserAndExchange(user, account.getExchange())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!userAccount.isAdmin() && !account.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		List<TransactionData> transactionData = transactionRepository.findAllByAccountNumberOrderByTimestampDesc(account.getNumber(), PageRequest.of(0, 5))
				.map(TransactionData::new)
				.stream().toList();
		return new FullAccountData(
				account.getId(),
				account.getNumber(),
				account.getName(),
				account.isAdmin(),
				userAccount.isAdmin(),
				new ExchangeData(
						account.getExchange().getId(),
						account.getExchange().getName(),
						account.getExchange().getPrimaryTradeable().getSymbol()
				),
				account.getBalances().stream()
						.map(b -> new BalanceData(b.getTradeable().getId(), b.getTradeable().getSymbol(), b.getAmount().toPlainString()))
						.sorted(Comparator.comparing(BalanceData::symbol))
						.toList(),
				account.getTotalBalance().toPlainString(),
				transactionData
		);
	}

	@Transactional
	public void editBalances(long accountId, User user, MultiValueMap<String, String> paramMap) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Account userAccount = accountRepository.findByUserAndExchange(user, account.getExchange())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!userAccount.isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		for (var entry : paramMap.entrySet()) {
			if (entry.getKey().startsWith("tradeable-")) {
				long tradeableId = Long.parseLong(entry.getKey().substring(10));
				Tradeable tradeable = tradeableRepository.findById(tradeableId)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
				BigDecimal value = new BigDecimal(entry.getValue().get(0));
				Balance bal = account.getBalanceForTradeable(tradeable);
				if (bal == null) {
					bal = new Balance(account, tradeable, value);
					account.getBalances().add(bal);
				} else {
					bal.setAmount(value);
				}
			}
		}
		accountRepository.save(account);
	}
}
