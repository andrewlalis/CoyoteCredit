package nl.andrewl.coyotecredit.ctl.dto;

public record AddAccountPayload(
		String name,
		String username,
		String password
) {}
