package nl.andrewl.coyotecredit.ctl.exchange.dto;

public record TransferPayload (
		String recipientNumber,
		String amount,
		long tradeableId,
		String message
) {}
