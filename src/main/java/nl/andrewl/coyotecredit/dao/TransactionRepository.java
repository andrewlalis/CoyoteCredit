package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	Page<Transaction> findAllByAccountNumberOrderByTimestampDesc(String accountNumber, Pageable pageable);
}
