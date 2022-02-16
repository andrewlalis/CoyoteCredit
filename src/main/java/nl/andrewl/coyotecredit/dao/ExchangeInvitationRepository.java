package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.ExchangeInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeInvitationRepository extends JpaRepository<ExchangeInvitation, Long> {
	Optional<ExchangeInvitation> findByCode(String code);

	List<ExchangeInvitation> findAllByUserEmail(String email);
}
