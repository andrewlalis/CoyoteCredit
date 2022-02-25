package nl.andrewl.coyotecredit.ctl.exchange.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public record EditExchangePayload(
		@NotNull @NotBlank
		String name,
		String description,
		boolean publiclyAccessible,
		@PositiveOrZero
		long primaryTradeableId,

		// Colors
		String primaryBackgroundColor,
		String secondaryBackgroundColor,
		String primaryForegroundColor,
		String secondaryForegroundColor
) {}
