package nl.andrewl.coyotecredit.ctl.dto;

public record RequestExchangePayload (
		int estimatedAccountCount,
		String organization,
		String reason
) {}
