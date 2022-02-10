package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.dao.AccountRepository;
import nl.andrewl.coyotecredit.model.Account;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

	@Transactional(readOnly = true)
	public AccountData getAccountData(User user, long accountId) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return new AccountData(account.getId(), account.getNumber(), account.getExchange().getName());
	}
}
