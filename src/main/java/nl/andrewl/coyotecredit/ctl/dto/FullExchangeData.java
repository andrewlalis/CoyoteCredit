package nl.andrewl.coyotecredit.ctl.dto;

import java.util.List;

/**
 * The data that's used on the exchange page.
 */
public record FullExchangeData (
		long id,
		String name,
		TradeableData primaryTradeable,
		List<TradeableData> supportedTradeables,
		String totalMarketValue,
		int accountCount,
		// Account info that's needed for determining if it's possible to do some actions.
		boolean accountAdmin,
		long accountId
) {}
