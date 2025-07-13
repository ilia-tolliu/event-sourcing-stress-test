package itolliu.esstress.simulation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Operation(
        UUID txId,
        UUID accountId,
        String name,
        BigDecimal debit,
        BigDecimal credit,
        Instant createdAt
) {
}

