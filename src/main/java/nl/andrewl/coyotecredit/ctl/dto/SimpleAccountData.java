package nl.andrewl.coyotecredit.ctl.dto;

public record SimpleAccountData (
		long id,
		String number,
		String name,
		boolean admin,
		String totalBalance
) {
}
