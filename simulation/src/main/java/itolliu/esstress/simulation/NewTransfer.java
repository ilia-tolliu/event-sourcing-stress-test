package itolliu.esstress.simulation;

import java.math.BigDecimal;
import java.util.UUID;

public record NewTransfer(
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount
) {
}
