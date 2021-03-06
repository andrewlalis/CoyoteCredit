package nl.andrewl.coyotecredit.ctl.exchange.dto;

import java.util.List;

/**
 * The data that's needed for displaying a user's account page.
 */
public record FullAccountData (
		long id,
		String number,
		String name,
		boolean admin,
		long userId,
		String username,
		boolean userAdmin,// If the current user is an admin of the exchange this account is in.
		boolean userIsOwner,// If the current user is the owner of this account.
		ExchangeData exchange,
		List<BalanceData> balances,
		String totalBalance,
		List<TransactionData> recentTransactions
) {
}
