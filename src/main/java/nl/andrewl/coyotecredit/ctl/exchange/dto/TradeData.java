package nl.andrewl.coyotecredit.ctl.exchange.dto;

import nl.andrewl.coyotecredit.ctl.dto.TradeableData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record TradeData(
		long accountId,
		String accountNumber,
		long exchangeId,
		List<TradeableData> tradeablesToSell,
		Map<String, BigDecimal> accountBalances,
		List<TradeableData> tradeablesToBuy
) {}
