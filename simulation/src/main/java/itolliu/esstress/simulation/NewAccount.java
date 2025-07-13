package itolliu.esstress.simulation;

import java.math.BigDecimal;
import java.util.UUID;

public record NewAccount(
        UUID accountId,
        BigDecimal openingBalance
) {
}
