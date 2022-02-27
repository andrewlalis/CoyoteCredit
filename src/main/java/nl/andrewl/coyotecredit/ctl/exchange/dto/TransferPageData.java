package nl.andrewl.coyotecredit.ctl.exchange.dto;

import java.util.List;

public record TransferPageData(
		long exchangeId,
		List<BalanceData> balances
) {
}
