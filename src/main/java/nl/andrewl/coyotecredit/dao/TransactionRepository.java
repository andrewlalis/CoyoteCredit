package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.Tradeable;
import nl.andrewl.coyotecredit.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	Page<Transaction> findAllByAccountNumberOrderByTimestampDesc(String accountNumber, Pageable pageable);

	@Modifying
	@Query("DELETE FROM Transaction tx WHERE tx.from = :t OR tx.to = :t")
	void deleteAllWithTradeable(Tradeable t);
}
