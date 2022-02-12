package nl.andrewl.coyotecredit.ctl.dto;

import nl.andrewl.coyotecredit.model.Tradeable;

public record TradeableData(
		long id,
		String symbol,
		String type,
		String marketPriceUsd,
		String name,
		String description
) {
	public TradeableData(Tradeable t) {
		this(
				t.getId(),
				t.getSymbol(),
				t.getType().name(),
				t.getMarketPriceUsd().toPlainString(),
				t.getName(),
				t.getDescription()
		);
	}
}
