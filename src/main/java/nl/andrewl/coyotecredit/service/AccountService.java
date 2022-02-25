package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.*;
import nl.andrewl.coyotecredit.ctl.exchange.dto.ExchangeData;
import nl.andrewl.coyotecredit.dao.*;
import nl.andrewl.coyotecredit.model.*;
import nl.andrewl.coyotecredit.util.AccountNumberUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final TradeableRepository tradeableRepository;
	private final TransferRepository transferRepository;
	private final AccountValueSnapshotRepository valueSnapshotRepository;
	private final UserNotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public List<BalanceData> getTransferData(long accountId, User user) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!account.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return account.getBalances().stream()
				.filter(b -> b.getAmount().compareTo(BigDecimal.ZERO) > 0)
				.map(b -> new BalanceData(
						b.getTradeable().getId(),
						b.getTradeable().getSymbol(),
						b.getTradeable().getType().name(),
						b.getAmount().toPlainString()
				))
				.sorted(Comparator.comparing(BalanceData::symbol))
				.toList();
	}

	@Transactional
	public void transfer(long accountId, User user, TransferPayload payload) {
		Account sender = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!sender.getUser().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		String recipientNumber = payload.recipientNumber().trim();
		if (!AccountNumberUtils.isValid(recipientNumber)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"The recipient number \"" + recipientNumber + "\" is not valid. Should be of the form 1234-1234-1234-1234."
			);
		}
		Account recipient = accountRepository.findByNumber(recipientNumber)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown recipient."));
		if (!recipient.getExchange().getId().equals(sender.getExchange().getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer funds between exchanges.");
		}
		Tradeable tradeable = tradeableRepository.findById(payload.tradeableId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown tradeable asset."));
		BigDecimal amount = new BigDecimal(payload.amount());
		if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount. Should be positive.");
		Balance senderBalance = sender.getBalanceForTradeable(tradeable);
		if (senderBalance == null || senderBalance.getAmount().compareTo(amount) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough funds to transfer.");
		}
		Balance recipientBalance = recipient.getBalanceForTradeable(tradeable);
		if (recipientBalance == null) {
			recipientBalance = new Balance(recipient, tradeable, BigDecimal.ZERO);
			recipient.getBalances().add(recipientBalance);
		}
		senderBalance.setAmount(senderBalance.getAmount().subtract(amount));
		recipientBalance.setAmount(recipientBalance.getAmount().add(amount));
		accountRepository.save(sender);
		accountRepository.save(recipient);
		Transfer t = transferRepository.save(new Transfer(
				sender.getNumber(),
				recipient.getNumber(),
				tradeable,
				amount,
				payload.message()
		));
		String senderMessage = String.format("You have sent %s %s to %s (%s) from your account in %s.",
				amount.toPlainString(),
				tradeable.getSymbol(),
				recipient.getNumber(),
				recipient.getName(),
				sender.getExchange().getName());
		String recipientMessage = String.format("You have received %s %s from %s (%s) in your account in %s.",
				amount.toPlainString(),
				tradeable.getSymbol(),
				sender.getNumber(),
				sender.getName(),
				recipient.getExchange().getName());
		if (t.getMessage() != null) {
			recipientMessage += " Message: " + t.getMessage();
			senderMessage += " Message: " + t.getMessage();
		}
		notificationRepository.save(new UserNotification(sender.getUser(), senderMessage));
		notificationRepository.save(new UserNotification(recipient.getUser(), recipientMessage));
	}

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
				account.getUser().getId().equals(user.getId()),
				new ExchangeData(
						account.getExchange().getId(),
						account.getExchange().getName(),
						account.getExchange().getDescription(),
						account.getExchange().getPrimaryTradeable().getSymbol(),
						account.getExchange().getPrimaryTradeable().getId()
				),
				account.getBalances().stream()
						.map(b -> new BalanceData(
								b.getTradeable().getId(),
								b.getTradeable().getSymbol(),
								b.getTradeable().getType().name(),
								b.getAmount().toPlainString()
						))
						.sorted(Comparator.comparing(BalanceData::amount).reversed().thenComparing(BalanceData::symbol))
						.toList(),
				TradeableData.DECIMAL_FORMAT.format(account.getTotalBalance()),
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
		notificationRepository.save(new UserNotification(
				account.getUser(),
				String.format("Your account %s in %s had its balances edited by %s.",
						account.getNumber(),
						account.getExchange().getName(),
						userAccount.getName())
		));
	}

	@Scheduled(cron = "@midnight")
	@Transactional
	public void doAccountValueSnapshots() {
		Pageable pageable = Pageable.ofSize(10);
		LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
		Page<Account> page = accountRepository.findAll(pageable);
		while (!page.isEmpty()) {
			List<AccountValueSnapshot> snapshots = new ArrayList<>();
			for (var account : page.getContent()) {
				snapshots.add(new AccountValueSnapshot(account, timestamp, account.getTotalBalanceUsd()));
			}
			valueSnapshotRepository.saveAll(snapshots);
			pageable = pageable.next();
			page = accountRepository.findAll(pageable);
		}
	}
}
