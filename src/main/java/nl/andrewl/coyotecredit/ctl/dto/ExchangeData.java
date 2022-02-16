package nl.andrewl.coyotecredit.ctl.dto;

public record ExchangeData(
		long id,
		String name,
		String description,
		String primaryTradeable,
		long primaryTradeableId
) {
}
