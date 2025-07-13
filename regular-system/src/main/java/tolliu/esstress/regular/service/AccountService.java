package tolliu.esstress.regular.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tolliu.esstress.regular.domain.Account;
import tolliu.esstress.regular.domain.NewAccount;
import tolliu.esstress.regular.domain.NewOperation;
import tolliu.esstress.regular.domain.NewTransfer;
import tolliu.esstress.regular.repo.AccountRepo;
import tolliu.esstress.regular.repo.OperationRepo;

import java.math.BigDecimal;
import java.util.UUID;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static tolliu.esstress.regular.domain.NewTransfer.FEE_AMOUNT;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepo accountRepo;

    public final OperationRepo operationRepo;

    public final TransactionTemplate transactions;

    public Account openAccount(NewAccount newAccount, BigDecimal amount) {
        var account = transactions.execute(tx -> {
            var acc = accountRepo.insertAccount(newAccount);

            var txId = UUID.randomUUID();
            var newOperation = new NewOperation(
                    txId,
                    acc.id(),
                    "open account",
                    amount,
                    ZERO
            );

            operationRepo.insertOperation(newOperation);

            return acc;
        });

        return account;
    }

    public UUID transfer(NewTransfer newTransfer) {
        var transactionId = transactions.execute(tx -> {
            var sourceAccountBalance = accountRepo.retrieveBalance(newTransfer.sourceAccountId())
                    .orElseThrow(() -> new RuntimeException(format("Account not found: %s", newTransfer.sourceAccountId())));

            var balanceAfterTransfer = sourceAccountBalance.subtract(newTransfer.amount()).subtract(FEE_AMOUNT);

            if (balanceAfterTransfer.compareTo(ZERO) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            var txId = UUID.randomUUID();

            operationRepo.insertOperation(newTransfer.creditOperation(txId));
            operationRepo.insertOperation(newTransfer.debitOperation(txId));
            operationRepo.insertOperation(newTransfer.feeChargeOperation(txId));
            operationRepo.insertOperation(newTransfer.feeCollectOperation(txId));

            return txId;
        });

        return transactionId;
    }

    public BigDecimal balance(UUID accountId) {
        var balance = accountRepo.retrieveBalance(accountId)
                .orElseThrow(() -> new RuntimeException(format("Account not found: %s", accountId)));

        return balance;
    }
}
