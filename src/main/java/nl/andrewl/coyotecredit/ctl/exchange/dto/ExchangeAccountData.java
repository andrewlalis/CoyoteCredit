package nl.andrewl.coyotecredit.ctl.exchange.dto;

public record ExchangeAccountData(
		ExchangeData exchange,
		SimpleAccountData account
) {}
