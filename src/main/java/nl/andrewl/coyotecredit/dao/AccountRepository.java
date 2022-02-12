package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.Account;
import nl.andrewl.coyotecredit.model.Exchange;
import nl.andrewl.coyotecredit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	List<Account> findAllByUser(User user);
	Optional<Account> findByNumber(String number);
	Optional<Account> findByUserAndExchange(User user, Exchange exchange);
}
