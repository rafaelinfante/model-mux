package net.rafaelinfante.modelmux.token;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByClientIdAndWindowDate(String clientId, LocalDate windowDate);

    @Modifying
    @Query(
            """
            update Budget b
            set b.spentUsd = b.spentUsd + :amount
            where b.clientId = :clientId and b.windowDate = :windowDate
            """)
    int addSpend(
            @Param("clientId") String clientId,
            @Param("windowDate") LocalDate windowDate,
            @Param("amount") BigDecimal amount);
}
