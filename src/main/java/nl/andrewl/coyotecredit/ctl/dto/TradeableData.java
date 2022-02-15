package nl.andrewl.coyotecredit.ctl.dto;

import nl.andrewl.coyotecredit.model.Tradeable;

import java.text.DecimalFormat;

public record TradeableData(
		long id,
		String symbol,
		String type,
		String marketPriceUsd,
		String formattedPriceUsd,
		String name,
		String description,

		Long exchangeId,
		String exchangeName
) {
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

	public TradeableData(Tradeable t) {
		this(
				t.getId(),
				t.getSymbol(),
				t.getType().name(),
				t.getMarketPriceUsd().toPlainString(),
				DECIMAL_FORMAT.format(t.getMarketPriceUsd()),
				t.getName(),
				t.getDescription(),
				t.getExchange() == null ? null : t.getExchange().getId(),
				t.getExchange() == null ? null : t.getExchange().getName()
		);
	}
}
