package tolliu_ilia.esstress.regular_system.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tolliu_ilia.esstress.regular_system.domain.Account;
import tolliu_ilia.esstress.regular_system.domain.NewAccount;
import tolliu_ilia.esstress.regular_system.service.AccountService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountsController {

    private final AccountService accountService;

    @PostMapping(path = "")
    @ResponseStatus(OK)
    OpenAccountResponse openAccount(@RequestBody @Valid OpenAccountRequest json) {
        var newAccount = new NewAccount();
        var account = accountService.openAccount(newAccount, json.amount());

        return new OpenAccountResponse(account);
    }

    record OpenAccountRequest(
            @NotNull
            BigDecimal amount
    ) {
    }

    record OpenAccountResponse(Account account) {
    }

    @GetMapping("/{accountId}/balance")
    @ResponseStatus(OK)
    BalanceResponse balance(@PathVariable("accountId") UUID accountId) {
        var balance = accountService.balance(accountId);

        return new BalanceResponse(balance);
    }

    record BalanceResponse(
            BigDecimal balance
    ) {
    }
}
