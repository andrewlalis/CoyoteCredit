package nl.andrewl.coyotecredit.ctl.exchange.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record InviteUserPayload(
		@NotBlank @NotNull
		String email
) {
}
