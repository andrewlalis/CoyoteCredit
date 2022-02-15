package nl.andrewl.coyotecredit.service;

import lombok.RequiredArgsConstructor;
import nl.andrewl.coyotecredit.ctl.dto.TradeableData;
import nl.andrewl.coyotecredit.dao.AccountRepository;
import nl.andrewl.coyotecredit.dao.TradeableRepository;
import nl.andrewl.coyotecredit.model.Tradeable;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TradeableService {
	private final TradeableRepository tradeableRepository;
	private final AccountRepository accountRepository;

	@Transactional(readOnly = true)
	public TradeableData getTradeable(long id, User user) {
		Tradeable tradeable = tradeableRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (tradeable.getExchange() != null && !accountRepository.existsByUserAndExchange(user, tradeable.getExchange())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return new TradeableData(tradeable);
	}
}
