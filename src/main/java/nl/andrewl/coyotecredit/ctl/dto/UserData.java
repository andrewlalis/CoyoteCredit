package nl.andrewl.coyotecredit.ctl.dto;

import java.util.List;

public record UserData (
		long id,
		String username,
		String email,
		List<InvitationData> exchangeInvitations
) {}
