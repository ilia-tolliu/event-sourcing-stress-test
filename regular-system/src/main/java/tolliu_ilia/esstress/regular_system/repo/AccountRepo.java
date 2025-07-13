package tolliu_ilia.esstress.regular_system.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tolliu_ilia.esstress.regular_system.domain.Account;
import tolliu_ilia.esstress.regular_system.domain.NewAccount;

import java.util.Map;
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
                gen_random_uuid(),
                CURRENT_TIMESTAMP
              )
              RETURNING *
              """,
              Map.of(),
              (rs, i) -> new Account(
                      rs.getObject("accounts_id", UUID.class),
                      rs.getTimestamp("accounts_created_at").toInstant()
              )
      );

      return result;
    };
}
