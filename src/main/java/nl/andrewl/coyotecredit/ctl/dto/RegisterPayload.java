package nl.andrewl.coyotecredit.ctl.dto;

public record RegisterPayload (
		String username,
		String email,
		String password,
		// Invite data, this may be null.
		String inviteCode,
		String accountName
) {}
