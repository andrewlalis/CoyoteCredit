package nl.andrewl.coyotecredit.ctl.dto;

import java.util.List;

public record EditTradeablesData(
		List<TradeableData> supportedPublicTradeables,
		List<TradeableData> customTradeables,
		TradeableData primaryTradeable,
		List<TradeableData> eligiblePublicTradeables
) {}
