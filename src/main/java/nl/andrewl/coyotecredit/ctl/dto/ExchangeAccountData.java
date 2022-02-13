package nl.andrewl.coyotecredit.ctl.dto;

public record ExchangeAccountData(
		ExchangeData exchange,
		SimpleAccountData account
) {}
