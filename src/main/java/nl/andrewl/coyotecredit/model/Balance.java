package nl.andrewl.coyotecredit.model;

import lombok.Getter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "account_balance")
@Getter
public class Balance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Account account;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private Currency currency;

	@Column(nullable = false, precision = 24, scale = 10)
	private BigDecimal amount;
}
