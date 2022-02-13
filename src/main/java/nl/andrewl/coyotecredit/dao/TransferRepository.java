package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
	@Query(
			"SELECT t FROM Transfer t " +
			"WHERE t.senderNumber = :accountNumber OR t.recipientNumber = :accountNumber " +
			"ORDER BY t.timestamp DESC"
	)
	Page<Transfer> findAllForAccount(String accountNumber, Pageable pageable);
}
