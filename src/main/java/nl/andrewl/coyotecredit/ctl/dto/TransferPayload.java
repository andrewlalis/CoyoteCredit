package nl.andrewl.coyotecredit.ctl.dto;

public record TransferPayload (
		String recipientNumber,
		String amount,
		long tradeableId,
		String message
) {}
