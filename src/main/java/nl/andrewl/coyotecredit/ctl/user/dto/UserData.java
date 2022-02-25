package nl.andrewl.coyotecredit.ctl.user.dto;

import nl.andrewl.coyotecredit.ctl.exchange.dto.InvitationData;

import java.util.List;

public record UserData (
		long id,
		String username,
		String email,
		List<InvitationData> exchangeInvitations,
		List<UserNotificationData> newNotifications
) {}
