package nl.andrewl.coyotecredit.ctl.exchange.dto;

public record AddAccountPayload(
		String username,
		String password,
		String email,
		String accountName
) {}
