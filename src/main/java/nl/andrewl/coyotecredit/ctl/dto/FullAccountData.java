package nl.andrewl.coyotecredit.ctl.dto;

import java.util.List;

/**
 * The data that's needed for displaying a user's account page.
 */
public record FullAccountData (
		long id,
		String number,
		String name,
		boolean admin,
		boolean userAdmin,
		ExchangeData exchange,
		List<BalanceData> balances,
		String totalBalance,
		List<TransactionData> recentTransactions
) {
}
