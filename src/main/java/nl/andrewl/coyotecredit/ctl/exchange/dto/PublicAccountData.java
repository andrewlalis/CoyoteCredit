package nl.andrewl.coyotecredit.ctl.exchange.dto;

/**
 * Account data that can be publicly seen by any member of an exchange.
 */
public record PublicAccountData(
		long id,
		String number,
		String name
) {}
