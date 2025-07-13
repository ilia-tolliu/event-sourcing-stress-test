package tolliu.esstress.regular.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tolliu.esstress.regular.domain.Account;
import tolliu.esstress.regular.domain.NewAccount;
import tolliu.esstress.regular.domain.NewTransfer;
import tolliu.esstress.regular.service.AccountService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PutMapping(path = "/{accountId}")
    @ResponseStatus(OK)
    OpenAccountResponse openAccount(@PathVariable("accountId") UUID accountId, @RequestBody @Valid OpenAccountRequest json) {
        var newAccount = new NewAccount(accountId);
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

    @PostMapping("/{accountId}/transfer")
    @ResponseStatus(CREATED)
    TransferResponse transfer(@PathVariable("accountId") UUID sourceAccountId, @RequestBody @Valid TransferRequest json) {
        var newTransfer = new NewTransfer(
                sourceAccountId,
                json.destinationAccountId(),
                json.amount()
        );

        var transactionId = accountService.transfer(newTransfer);

        return new TransferResponse(transactionId);
    }

    record TransferRequest(
            UUID destinationAccountId,
            BigDecimal amount
    ) {
    }

    record TransferResponse(
            UUID transactionId
    ) {
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
