package itolliu.esstress.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

public record Client(
        HttpClient http,
        ObjectMapper json,
        URI baseUrl
) {
    public void openAccount(NewAccount newAccount) {
        try {
            var url = baseUrl.resolve(format("/accounts/%s", newAccount.accountId()));

            var requestBody = json.writeValueAsString(
                    Map.of(
                            "amount", newAccount.openingBalance()
                    )
            );

            var request = HttpRequest.newBuilder()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Unexpected status code " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BigDecimal balance(UUID accountId) {
        try {
            var url = baseUrl.resolve(format("/accounts/%s/balance", accountId));

            var request = HttpRequest.newBuilder()
                    .uri(url)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Unexpected status code " + response.statusCode());
            }

            var balanceResponse = json.readValue(response.body(), BalanceResponse.class);

            return balanceResponse.balance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    record BalanceResponse(
            BigDecimal balance
    ) {
    }

    public List<Operation> operations(UUID accountId) {
        try {
            var url = baseUrl.resolve(format("/accounts/%s/operations", accountId));

            var request = HttpRequest.newBuilder()
                    .uri(url)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Unexpected status code " + response.statusCode());
            }

            var operationsResponse = json.readValue(response.body(), OperationsResponse.class);

            return operationsResponse.operations;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    record OperationsResponse(
            List<Operation> operations
    ) { }

    public boolean transfer(NewTransfer newTransfer) {
        try {
            var url = baseUrl.resolve(format("/accounts/%s/transfer", newTransfer.sourceAccountId()));

            var requestBody = json.writeValueAsString(
                    Map.of(
                            "destinationAccountId", newTransfer.destinationAccountId(),
                            "amount", newTransfer.amount()
                    )
            );

            var request = HttpRequest.newBuilder()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                return true;
            }
            if (response.statusCode() == 409) {
                return false;
            }

            throw new RuntimeException("Unexpected status code " + response.statusCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
