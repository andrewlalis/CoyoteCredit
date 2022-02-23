package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.Account;
import nl.andrewl.coyotecredit.model.AccountValueSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountValueSnapshotRepository extends JpaRepository<AccountValueSnapshot, Long> {

	void deleteAllByAccount(Account account);
}
