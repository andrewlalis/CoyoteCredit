package nl.andrewl.coyotecredit.ctl.dto;

public record RegisterPayload (
		String username,
		String email,
		String password
) {}
