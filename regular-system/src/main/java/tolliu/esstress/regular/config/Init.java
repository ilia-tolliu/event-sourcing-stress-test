package tolliu.esstress.regular.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tolliu.esstress.regular.domain.NewAccount;
import tolliu.esstress.regular.domain.NewOperation;
import tolliu.esstress.regular.repo.AccountRepo;
import tolliu.esstress.regular.repo.OperationRepo;

import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static tolliu.esstress.regular.domain.NewTransfer.FEE_ACCOUNT_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Init implements InitializingBean {

    public final TransactionTemplate transactions;

    private final OperationRepo operationRepo;

    private final AccountRepo accountRepo;

    @Override
    public void afterPropertiesSet() {
        initFeeAccount();
    }

    private void initFeeAccount() {
        transactions.executeWithoutResult(tx -> {
            var isFeeAccountOpened = accountRepo.retrieveAccount(FEE_ACCOUNT_ID).isPresent();

            if (isFeeAccountOpened) {
                return;
            }

            accountRepo.insertAccount(new NewAccount(FEE_ACCOUNT_ID));

            operationRepo.insertOperation(new NewOperation(
                    UUID.randomUUID(),
                    FEE_ACCOUNT_ID,
                    "open fee account",
                    ZERO,
                    ZERO
            ));
        });
    }
}
