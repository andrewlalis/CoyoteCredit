package nl.andrewl.coyotecredit.ctl.exchange.dto;

import nl.andrewl.coyotecredit.ctl.dto.TradeableData;
import nl.andrewl.coyotecredit.model.Transfer;

import java.time.format.DateTimeFormatter;

public record TransferData(
		long id,
		String timestamp,
		String senderNumber,
		String recipientNumber,
		TradeableData tradeable,
		String amount,
		String message
) {
	public TransferData(Transfer t) {
		this(
				t.getId(),
				t.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC",
				t.getSenderNumber(),
				t.getRecipientNumber(),
				new TradeableData(t.getTradeable()),
				t.getAmount().toPlainString(),
				t.getMessage()
		);
	}
}
