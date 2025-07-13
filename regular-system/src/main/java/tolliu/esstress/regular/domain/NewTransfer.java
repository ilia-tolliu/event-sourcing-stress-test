package tolliu.esstress.regular.domain;

import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;

public record NewTransfer(
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount
) {

    public final static UUID FEE_ACCOUNT_ID = UUID.fromString("9af05502-fa7b-4f85-9bf3-2459ed88f216");

    public final static BigDecimal FEE_AMOUNT = new BigDecimal("0.01");

    public NewOperation creditOperation(UUID txId) {
        return new NewOperation(txId, sourceAccountId(), "outgoing transfer", ZERO, amount());
    }

    public NewOperation debitOperation(UUID txId) {
        return new NewOperation(txId, destinationAccountId(), "incoming transfer", amount, ZERO);
    }

    public NewOperation feeChargeOperation(UUID txId) {
        return new NewOperation(txId, sourceAccountId(), "fee charge", ZERO, FEE_AMOUNT);
    }

    public NewOperation feeCollectOperation(UUID txId) {
        return new NewOperation(txId, FEE_ACCOUNT_ID, "fee collect", FEE_AMOUNT, ZERO);
    }
}
