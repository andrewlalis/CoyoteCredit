package nl.andrewl.coyotecredit.ctl.dto;

public record AddAccountPayload(
		String name,
		String email,
		String username,
		String password
) {}
