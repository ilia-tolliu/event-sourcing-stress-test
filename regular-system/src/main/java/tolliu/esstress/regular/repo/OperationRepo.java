package tolliu.esstress.regular.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tolliu.esstress.regular.domain.NewOperation;
import tolliu.esstress.regular.domain.Operation;

import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OperationRepo {

    private final NamedParameterJdbcTemplate jdbc;

    public Operation insertOperation(NewOperation newOperation) {
        var result = jdbc.queryForObject(
                """
                        INSERT INTO operations (
                          operations_tx_id,
                          accounts_id,
                          operations_name,
                          operations_debit,
                          operations_credit,
                          operations_created_at
                        )
                        VALUES (
                          :txId,
                          :accountId,
                          :name,
                          :debit,
                          :credit,
                          CURRENT_TIMESTAMP
                        )
                        RETURNING *
                        """,
                Map.of(
                        "txId", newOperation.txId(),
                        "accountId", newOperation.accountId(),
                        "name", newOperation.name(),
                        "debit", newOperation.debit(),
                        "credit", newOperation.credit()
                ),
                (rs, i) -> new Operation(
                        rs.getObject("operations_tx_id", UUID.class),
                        rs.getObject("accounts_id", UUID.class),
                        rs.getString("operations_name"),
                        rs.getBigDecimal("operations_debit"),
                        rs.getBigDecimal("operations_credit"),
                        rs.getTimestamp("operations_created_at").toInstant()
                )
        );

        return result;
    }
}
