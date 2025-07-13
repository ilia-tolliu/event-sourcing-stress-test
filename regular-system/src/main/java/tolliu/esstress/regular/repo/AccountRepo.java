package tolliu.esstress.regular.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tolliu.esstress.regular.domain.Account;
import tolliu.esstress.regular.domain.NewAccount;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepo {

    private final NamedParameterJdbcTemplate jdbc;

    public Account insertAccount(NewAccount newAccount) {
        var result = jdbc.queryForObject(
                """
                        INSERT INTO accounts (
                          accounts_id,
                          accounts_created_at
                        )
                        VALUES (
                          :accountId,
                          CURRENT_TIMESTAMP
                        )
                        RETURNING *
                        """,
                Map.of(
                        "accountId", newAccount.accountId()
                ),
                AccountRepo::mapAccount
        );

        return result;
    }

    public Optional<Account> retrieveAccount(UUID accountId) {
        var result = jdbc.query(
                """
                        SELECT *
                        FROM accounts
                        WHERE accounts_id = :accountId
                        """,
                Map.of(
                        "accountId", accountId
                ),
                AccountRepo::mapAccount
        );

        return result.stream().findFirst();
    }

    public Optional<BigDecimal> retrieveBalance(UUID accountId) {
        var result = jdbc.query(
                """
                        SELECT
                            SUM(operations_debit) AS debit,
                            SUM(operations_credit) AS credit
                        FROM operations
                        WHERE accounts_id = :accountId
                        GROUP BY accounts_id
                        """,
                Map.of("accountId", accountId),
                (rs, i) -> {
                    var debit = rs.getBigDecimal("debit");
                    var credit = rs.getBigDecimal("credit");
                    return debit.subtract(credit);
                }
        );

        return result.stream().findFirst();
    }

    static Account mapAccount(ResultSet rs, int rowNum) throws SQLException {
        return new Account(
                rs.getObject("accounts_id", UUID.class),
                rs.getTimestamp("accounts_created_at").toInstant()
        );
    }
}
