package nl.andrewl.coyotecredit.ctl.exchange.dto;

import nl.andrewl.coyotecredit.ctl.dto.SimpleAccountData;

public record ExchangeAccountData(
		ExchangeData exchange,
		SimpleAccountData account
) {}
