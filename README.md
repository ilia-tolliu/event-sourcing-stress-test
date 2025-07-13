# event-sourcing-stress-test

This project models a typical EventSourcing problem,
transfer money between two bank accounts.

To make things a bit more realistic (and interesting),
a third account is involved: fee account. Hence, every transfer 
creates operations on three accounts: source, destination and fee.

This problem is solved in multiple ways.

**regular-system** uses a standard approach, where transfer happens
in scope of a single ACID transaction.

**simulation** generates a dozen of accounts and thousands of transfer.
After running the workload it compares expected and actual final balances. 
Also, it checks running balances (reconciliation report) whether it gets negative.

## Getting started

Clone the repository

Get Java 21

Run local PostgreSQL:

```shell
cd regular-system
docker compose up -d
```

Run regular-system:

```shell
./gradlew bootRun
```

Regular system starts on port 8081.

Run simulation in another shell session:

```shell
cd simulation
./gradlew run
```

Analyse the output.

By (un)commenting the relevant line in [simulation](https://github.com/ilia-tolliu/event-sourcing-stress-test/blob/b684b574d7f94ab2ae54b34d26b7ca5cd80aef4f/simulation/src/main/java/itolliu/esstress/simulation/App.java#L22)
you can use single-threaded or concurrent load. 
Correct work of single-threaded variant confirms that the algorithm implemented correctly.
Concurrent mode should reveal the issues, which is the main purpose of the simulation.