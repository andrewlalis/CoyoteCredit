package nl.andrewl.coyotecredit.ctl.exchange.dto;

public record BalanceData(
	long id,
	String symbol,
	String type,
	String amount
) {}
