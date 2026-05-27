package net.rafaelinfante.modelmux.token;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsageRepository extends JpaRepository<UsageRecord, Long> {

    @Query(
            """
            select new net.rafaelinfante.modelmux.token.UsageTotals(
                count(u),
                coalesce(sum(u.costUsd), 0),
                coalesce(sum(u.totalTokens), 0),
                coalesce(sum(case when u.cacheHit = true then 1 else 0 end), 0),
                coalesce(sum(case when u.failedOver = true then 1 else 0 end), 0))
            from UsageRecord u
            where u.createdAt >= :since
            """)
    UsageTotals totalsSince(@Param("since") Instant since);

    @Query(
            """
            select new net.rafaelinfante.modelmux.token.ProviderUsage(
                u.providerId,
                count(u),
                coalesce(sum(u.costUsd), 0),
                coalesce(sum(u.totalTokens), 0))
            from UsageRecord u
            where u.createdAt >= :since
            group by u.providerId
            order by count(u) desc
            """)
    List<ProviderUsage> perProviderSince(@Param("since") Instant since);
}
