# Banking Simulation

A desktop banking simulation built with Java Swing and MySQL. Customers can
register, sign in, view their balance and transaction history, deposit,
withdraw, transfer funds, manage beneficiaries, and review notifications.

## Prerequisites

- JDK 21 or later
- Maven 3.9 or later
- MySQL 8.0 or later

## Set up the database

Run [`schema.sql`](src/main/resources/database/schema.sql) in MySQL. For
example, from PowerShell:

```powershell
mysql -u root -p < src/main/resources/database/schema.sql
```

The application has no embedded database and never stores a database password
in source code. Configure a least-privileged MySQL user with either environment
variables:

```powershell
$env:BANKING_DB_URL = "jdbc:mysql://localhost:3306/banking_simulation"
$env:BANKING_DB_USER = "banking_app"
$env:BANKING_DB_PASSWORD = "choose-a-strong-password"
```

or JVM properties named `banking.db.url`, `banking.db.user`, and
`banking.db.password`. The URL defaults to
`jdbc:mysql://localhost:3306/banking_simulation`, and the user defaults to
`root` with an empty password only for local development.

## Run

```powershell
mvn clean package
java -jar target/BankingSimulation-1.0-SNAPSHOT.jar
```

`mvn package` creates a self-contained runnable JAR containing the MySQL and
BCrypt dependencies. The first screen allows a new customer to create a
Savings account, so no seed customer is required.

## Notes

- Money is represented as `DECIMAL(15,2)` in MySQL and `BigDecimal` in Java.
- Deposits, withdrawals, registrations, and transfers use database
  transactions; a failed operation is rolled back.
- This is a learning simulation, not production banking software. A real
  service also needs audited access controls, rate limiting, MFA, encryption
  and secret management, independent ledgering, observability, and formal
  security review.
