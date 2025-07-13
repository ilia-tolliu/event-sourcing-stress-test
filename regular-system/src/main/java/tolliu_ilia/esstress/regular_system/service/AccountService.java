package tolliu_ilia.esstress.regular_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tolliu_ilia.esstress.regular_system.domain.Account;
import tolliu_ilia.esstress.regular_system.domain.NewAccount;
import tolliu_ilia.esstress.regular_system.domain.NewOperation;
import tolliu_ilia.esstress.regular_system.repo.AccountRepo;
import tolliu_ilia.esstress.regular_system.repo.OperationRepo;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepo accountRepo;

    public final OperationRepo operationRepo;

    public final TransactionTemplate transactions;

    public Account openAccount(NewAccount newAccount, BigDecimal amount)  {
        var account = transactions.execute(tx -> {
            var acc = accountRepo.insertAccount(newAccount);

            var txId = UUID.randomUUID();
            var newOperation = new NewOperation(
                    txId,
                    acc.id(),
                    "open account",
                    amount,
                    BigDecimal.ZERO
            );

            operationRepo.insertOperation(newOperation);

            return acc;
        });

        return account;
    }
}
