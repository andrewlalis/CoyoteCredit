package nl.andrewl.coyotecredit.ctl.exchange.dto;

/**
 * The payload that's sent when a user performs a trade with the market. This
 * can be either a SELL or a BUY trade.
 *
 * <p>
 *     In a SELL trade, the user indicates an amount of their selling tradeable
 *     to sell in exchange for the market-equivalent value of the buying
 *     tradeable.
 * </p>
 * <p>
 *     In a BUY trade, the opposite happens, where the user indicates how much
 *     of the buying tradeable they want to acquire, and will have a market-
 *     equivalent value deducted from their selling tradeable.
 * </p>
 */
public record TradePayload(
		String type, // SELL or BUY
		long sellTradeableId,
		long buyTradeableId,
		String value
) {}
