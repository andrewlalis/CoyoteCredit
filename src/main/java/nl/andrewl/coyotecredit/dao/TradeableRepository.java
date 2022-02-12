package nl.andrewl.coyotecredit.dao;

import nl.andrewl.coyotecredit.model.Tradeable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeableRepository extends JpaRepository<Tradeable, Long> {
}
