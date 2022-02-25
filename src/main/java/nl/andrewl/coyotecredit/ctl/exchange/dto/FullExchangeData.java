package nl.andrewl.coyotecredit.ctl.exchange.dto;

import nl.andrewl.coyotecredit.ctl.dto.TradeableData;

import java.util.List;

/**
 * The data that's used on the exchange page.
 */
public record FullExchangeData (
		long id,
		String name,
		String description,
		boolean publiclyAccessible,
		TradeableData primaryTradeable,
		String primaryBackgroundColor,
		String secondaryBackgroundColor,
		String primaryForegroundColor,
		String secondaryForegroundColor,
		List<TradeableData> supportedTradeables,
		String totalMarketValue,
		int accountCount,
		// Account info that's needed for determining if it's possible to do some actions.
		boolean accountAdmin,
		long accountId
) {}
