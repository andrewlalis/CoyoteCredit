package nl.andrewl.coyotecredit.ctl.dto;

import nl.andrewl.coyotecredit.model.Transaction;

import java.time.format.DateTimeFormatter;

public record TransactionData(
		long id,
		TradeableData from,
		String fromAmount,
		TradeableData to,
		String toAmount,
		String timestamp
) {
	public TransactionData(Transaction t) {
		this(
				t.getId(),
				new TradeableData(t.getFrom()),
				t.getFromAmount().toPlainString(),
				new TradeableData(t.getTo()),
				t.getToAmount().toPlainString(),
				t.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
		);
	}
}
