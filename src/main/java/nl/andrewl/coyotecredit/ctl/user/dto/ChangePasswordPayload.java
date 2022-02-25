package nl.andrewl.coyotecredit.ctl.user.dto;

public record ChangePasswordPayload(
		String currentPassword,
		String newPassword
) {}
