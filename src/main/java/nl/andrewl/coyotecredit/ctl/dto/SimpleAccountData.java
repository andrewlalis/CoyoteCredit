package nl.andrewl.coyotecredit.ctl.dto;

public record SimpleAccountData (
		long id,
		long userId,
		String number,
		String name,
		boolean admin,
		String totalBalance
) {
}
