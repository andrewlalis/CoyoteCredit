package nl.andrewl.coyotecredit.ctl.exchange.dto;

import nl.andrewl.coyotecredit.ctl.dto.TradeableData;

import java.util.List;

public record EditTradeablesData(
		List<TradeableData> supportedPublicTradeables,
		List<TradeableData> customTradeables,
		TradeableData primaryTradeable,
		List<TradeableData> eligiblePublicTradeables
) {}
