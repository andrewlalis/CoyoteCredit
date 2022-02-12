package nl.andrewl.coyotecredit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CustomTradeable extends Tradeable {
	/**
	 * The exchange that the tradeable belongs to.
	 */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Exchange exchange;
}
