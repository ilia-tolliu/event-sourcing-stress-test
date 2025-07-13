package tolliu_ilia.esstress.regular_system.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record NewOperation(
        UUID txId,
        UUID accountId,
        String name,
        BigDecimal debit,
        BigDecimal credit
) {
}
