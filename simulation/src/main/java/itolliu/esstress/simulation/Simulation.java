package itolliu.esstress.simulation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.unmodifiableList;

public class Simulation {

    private final static UUID FEE_ACCOUNT_ID = UUID.fromString("9af05502-fa7b-4f85-9bf3-2459ed88f216");

    private final static BigDecimal FEE_AMOUNT = new BigDecimal("0.01");

    private final ConcurrentLinkedQueue<NewTransfer> successfulTransfers = new ConcurrentLinkedQueue<>();

    private final Map<UUID, BigDecimal> expectedFinalBalances = new HashMap<>();

    private final Map<UUID, BigDecimal> actualFinalBalances = new HashMap<>();

    private final Client client;

    private final ExecutorService executor;

    private final List<NewAccount> accounts;

    private final List<NewTransfer> transfers;

    private BigDecimal feeBalanceBefore = BigDecimal.ZERO;

    private BigDecimal totalBalanceBefore = BigDecimal.ZERO;

    private BigDecimal totalBalanceAfter = BigDecimal.ZERO;

    public Simulation(Client client, ExecutorService executor) {
        this.client = client;
        this.executor = executor;
        this.accounts = generateAccounts();
        this.transfers = generateTransfers(this.accounts);
    }

    public void run() {
        var start = Instant.now();

        openAccounts();
        totalBalanceBefore = getActualTotalBalance();

        runTransfers();

        calculateExpectedFinalBalances();
        getActualBalances();
        totalBalanceAfter = getActualTotalBalance();

        var finish = Instant.now();

        System.out.printf("Done in %s sec%n", Duration.between(start, finish).getSeconds());
    }

    public void checkFinalBalances() {
        System.out.printf("Total balance: %s -> %s%n", totalBalanceBefore, totalBalanceAfter);
        System.out.printf("Transfers succeeded: %s%n", successfulTransfers.size());

        for (var account : accounts) {
            var expectedBalance = expectedFinalBalances.get(account.accountId());
            var actualBalance = actualFinalBalances.get(account.accountId());
            if (expectedBalance.compareTo(actualBalance) != 0) {
                System.out.printf("Account [%s]; expected balance %s, but got %s%n", account.accountId(), expectedBalance, actualBalance);
            }
        }

        var expectedFeeBalance = expectedFinalBalances.get(FEE_ACCOUNT_ID);
        var actualFeeBalance = actualFinalBalances.get(FEE_ACCOUNT_ID);
        if (expectedFeeBalance.compareTo(actualFeeBalance) != 0) {
            System.out.printf("Fee account; expected balance %s, but got %s%n", expectedFeeBalance, actualFeeBalance);
        }
    }

    public void checkRunningBalances() {
        System.out.println("Checking running balances...");

        for(var account : accounts) {
            checkAccountRunningBalance(account.accountId());
        }

        System.out.println("Running balances checked.");
    }

    private void checkAccountRunningBalance(UUID accountId) {
        var runningBalance = BigDecimal.ZERO;

        var operations = client.operations(accountId);
        for (var operation : operations) {
            runningBalance = runningBalance.add(operation.debit()).subtract(operation.credit());
            if (runningBalance.compareTo(BigDecimal.ZERO) < 0) {
                System.out.printf("Account [%s], txId [%s]; running balance negative [%s]%n", operation.accountId(), operation.txId(), runningBalance);
                return;
            }
        }
    }

    private void openAccounts() {
        System.out.println("Opening accounts...");
        for (var account : accounts) {
            client.openAccount(account);
            totalBalanceBefore = totalBalanceBefore.add(account.openingBalance());
        }
        feeBalanceBefore = client.balance(FEE_ACCOUNT_ID);
        totalBalanceBefore = totalBalanceBefore.add(feeBalanceBefore);
        System.out.println("Accounts opened.");
    }

    private void runTransfers() {
        System.out.println("Running transfers...");
        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (var transfer : transfers) {
            futures.add(CompletableFuture.runAsync(() -> {
                var isSuccess = client.transfer(transfer);
                if (isSuccess) {
                    successfulTransfers.add(transfer);
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println("Transfers completed.");
    }

    private void calculateExpectedFinalBalances() {
        for (var account : accounts) {
            expectedFinalBalances.put(account.accountId(), account.openingBalance());
        }

        expectedFinalBalances.put(FEE_ACCOUNT_ID, feeBalanceBefore);

        for (var transfer : successfulTransfers) {
            expectedFinalBalances.compute(transfer.sourceAccountId(), (id, amount) -> {
                assert amount != null;
                return amount.subtract(transfer.amount()).subtract(FEE_AMOUNT);
            });
            expectedFinalBalances.compute(transfer.destinationAccountId(), (id, amount) -> {
                assert amount != null;
                return amount.add(transfer.amount());
            });
            expectedFinalBalances.compute(FEE_ACCOUNT_ID, (id, amount) -> {
                assert amount != null;
                return amount.add(FEE_AMOUNT);
            });
        }
    }

    private void getActualBalances() {
        System.out.println("Getting actual balances...");
        for (var account : accounts) {
            actualFinalBalances.put(account.accountId(), client.balance(account.accountId()));
        }

        actualFinalBalances.put(FEE_ACCOUNT_ID, client.balance(FEE_ACCOUNT_ID));
        System.out.println("Actual balances retrieved.");
    }

    private BigDecimal getActualTotalBalance() {
        System.out.println("Getting actual total balance...");
        var totalBalance = BigDecimal.ZERO;

        for (var account : accounts) {
            var balance = client.balance(account.accountId());
            totalBalance = totalBalance.add(balance);
        }
        var feeBalance = client.balance(FEE_ACCOUNT_ID);
        totalBalance = totalBalance.add(feeBalance);
        System.out.println("Actual total balance retrieved.");

        return totalBalance;
    }

    static List<NewAccount> generateAccounts() {
        System.out.println("Generating accounts...");
        var accounts = new ArrayList<NewAccount>();
        for (int i = 0; i < 12; i++) {
            accounts.add(new NewAccount(
                    UUID.randomUUID(),
                    BigDecimal.valueOf(new Random().nextLong(10_000L))
            ));
        }
        System.out.println("Accounts generated.");

        return unmodifiableList(accounts);
    }

    static List<NewTransfer> generateTransfers(List<NewAccount> accounts) {
        System.out.println("Generating transfers...");
        var transfers = new ArrayList<NewTransfer>();
        var random = new Random();

        while (transfers.size() < 100) {
            var sourceAccountIndex = random.nextInt(accounts.size());
            var destinationAccountIndex = random.nextInt(accounts.size());
            if (sourceAccountIndex == destinationAccountIndex) {
                continue;
            }

            var amount = random.nextLong(1000L);

            var transfer = new NewTransfer(
                    accounts.get(sourceAccountIndex).accountId(),
                    accounts.get(destinationAccountIndex).accountId(),
                    BigDecimal.valueOf(amount)
            );

            transfers.add(transfer);
        }
        System.out.println("Transfers generated.");

        return unmodifiableList(transfers);
    }

}
