package nl.andrewl.coyotecredit.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.dao.AccountRepository;
import nl.andrewl.coyotecredit.model.Account;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;

	public static record AccountData (
			long id,
			String accountNumber,
			String exchangeName
	) {}

	@Transactional(readOnly = true)
	public List<AccountData> getAccountsOverview(User user) {
		return accountRepository.findAllByUser(user).stream()
				.map(a -> new AccountData(a.getId(), a.getNumber(), a.getExchange().getName()))
				.toList();
	}

	public static class FullAccountData {
		public long id;
		public String number;
		public String exchangeName;
		public List<BalanceData> balances;
	}

	@AllArgsConstructor
	public static class BalanceData {
		public String currencyIdentifier;
		public String amount;
	}

	@Transactional(readOnly = true)
	public FullAccountData getAccountData(User user, long accountId) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!account.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		FullAccountData d = new FullAccountData();
		d.id = account.getId();
		d.number = account.getNumber();
		d.exchangeName = account.getExchange().getName();
		List<BalanceData> balanceData = new ArrayList<>();
		for (var bal : account.getBalances()) {
			balanceData.add(new BalanceData(bal.getCurrency().getIdentifier(), bal.getAmount().toPlainString()));
		}
		d.balances = balanceData;
		return d;
	}
}
