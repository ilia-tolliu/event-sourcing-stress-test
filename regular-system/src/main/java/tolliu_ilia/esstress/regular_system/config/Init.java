package tolliu_ilia.esstress.regular_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tolliu_ilia.esstress.regular_system.domain.NewOperation;
import tolliu_ilia.esstress.regular_system.repo.AccountRepo;
import tolliu_ilia.esstress.regular_system.repo.OperationRepo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static tolliu_ilia.esstress.regular_system.domain.NewTransfer.FEE_ACCOUNT_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Init implements InitializingBean {

    private final NamedParameterJdbcTemplate jdbc;

    public final TransactionTemplate transactions;

    private final OperationRepo operationRepo;

    private final AccountRepo accountRepo;

    @Override
    public void afterPropertiesSet() {
        initFeeAccount();
    }

    private void initFeeAccount() {
        transactions.executeWithoutResult(tx -> {
            var feeAccount = accountRepo.retrieveAccount(FEE_ACCOUNT_ID);

            if (feeAccount.isPresent()) {
                return;
            }

            jdbc.update(
                    """
                            INSERT INTO accounts (
                              accounts_id,
                              accounts_created_at
                            )
                            VALUES (
                              :feeAccountId::uuid,
                              CURRENT_TIMESTAMP
                            )
                            """,
                    Map.of(
                            "feeAccountId", FEE_ACCOUNT_ID
                    )
            );
            operationRepo.insertOperation(new NewOperation(
                    UUID.randomUUID(),
                    FEE_ACCOUNT_ID,
                    "open fee account",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            ));
        });
    }
}
