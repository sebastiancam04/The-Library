# Project Library: The Monolith

## Introduction

Project Library is a small library management system you run in your browser. It keeps track of the books in the
collection, the members who borrow them, and the loans and reservations that connect the two.

As a user you can:

- Browse the catalog of books and add new titles.
- Register members.
- Check out a book to a member and return it when they're done.
- Reserve a book and fulfill the reservation once a copy is available.
- See which loans are overdue and what late fees apply.

Everything runs in a single web app backed by a local SQLite database, so there is nothing else to install or configure:
start it and it's ready to use.

## Using the app

The browser UI is a single page with four tabs. Each tab has a small form at the top to create records, and a table
below that lists what already exists.

| Tab              | What you can do                                                                                                                                    |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| **Books**        | Add a book (title, author, ISBN, number of copies) and see total vs. available copies.                                                             |
| **Members**      | Register a member (name, email).                                                                                                                   |
| **Loans**        | Check out a book to a member by their IDs; return a loan with its **Return** button. The table shows due date, return status, and any overdue fee. |
| **Reservations** | Reserve a book by member and book IDs; turn a reservation into a loan with **Fulfill**.                                                            |

The page is plain HTML + Bulma CSS + a single `app.js`. It calls the same REST endpoints documented below and re-renders
the tables after each action, so there is no build step for the frontend.

## Stack

- **Backend**: [Javalin](https://javalin.io) 7 (Jetty 12)
- **Database**: SQLite (via `org.xerial:sqlite-jdbc`)
- **ORM**: [ORMLite](https://ormlite.com) 6.1
- **JSON**: [Jackson](https://github.com/FasterXML/jackson-databind)
- **Frontend**: [Bulma](https://bulma.io) CSS (CDN) + vanilla JavaScript
- **Build**: Gradle (`application` plugin)

## Architecture

A classic **monolith** with a layered structure. Requests flow one-way down the stack:

```
controller ──▶ service ──▶ dao ──▶ model (persisted via ORMLite)
```

| Layer      | Package         | Responsibility                                         |
|------------|-----------------|--------------------------------------------------------|
| Controller | `...controller` | HTTP routes, JSON (de)serialization, status codes      |
| Service    | `...service`    | Business rules (checkout, returns, fees, reservations) |
| DAO        | `...dao`        | ORMLite CRUD wrappers                                  |
| Model      | `...model`      | Domain entities mapped to SQLite tables                |
| Database   | `...db`         | Connection source, table creation, seed data           |

Wiring is done manually in `App.main`: DAOs → services → controllers, then each controller registers its own routes on a
shared `Javalin` instance. Static files in `src/main/resources/public` are served at `/`. A class diagram lives in
`docs/class-diagram.puml`.

## Project structure

```
src/main/java/cl/ucn/disc/arqsist/library/
├── App.java                      # entry point & manual DI wiring
├── controller/                   # Javalin route handlers
├── service/                      # business logic
├── dao/                          # ORMLite DAO wrappers
├── db/Database.java              # connection, tables, seeding
└── model/                        # Book, Member, Loan, Reservation
src/main/resources/public/        # static frontend (index.html, app.js)
src/test/java/...                 # JUnit 5 tests
docs/class-diagram.puml           # PlantUML class diagram
```

## Domain

- **Book** — `title`, `author`, `isbn`, `totalCopies`, `availableCopies`.
- **Member** — `name`, `email`.
- **Loan** — links a member to a book with `loanDate`, `dueDate`, `returnDate`, `returned`, `overdueFee`.
- **Reservation** — links a member to a book with `reservedAt`, `fulfilled`.

### Business rules:

- Checkout decrements `availableCopies` and sets a due date.
- Returning a loan increments `availableCopies`; overdue returns accrue a fee of 1.0 per day past due.
- A reservation can later be *fulfilled*, turning it into a loan.

## Run

```bash
./gradlew run
```

Then open http://localhost:7070

The SQLite database file `database.sqlite` is created in the project directory on first run and seeded with a few books
and members.

## REST API

| Method | Path                         | Description                                     |
|--------|------------------------------|-------------------------------------------------|
| GET    | `/books`                     | List all books                                  |
| POST   | `/books`                     | Add a book `{title, author, isbn, totalCopies}` |
| GET    | `/books/{id}`                | Get one book                                    |
| GET    | `/members`                   | List all members                                |
| POST   | `/members`                   | Register a member `{name, email}`               |
| GET    | `/loans`                     | List all loans                                  |
| POST   | `/loans`                     | Checkout `?memberId=&bookId=`                   |
| POST   | `/loans/{id}/return`         | Return a loan                                   |
| GET    | `/loans/overdue`             | List overdue loans                              |
| GET    | `/reservations`              | List all reservations                           |
| POST   | `/reservations`              | Reserve `?memberId=&bookId=`                    |
| POST   | `/reservations/{id}/fulfill` | Fulfill a reservation                           |

### Notes & examples

All responses are JSON. `POST` bodies are JSON; loan/reservation creation passes the IDs as query parameters instead of
a body.

Add a book:

```bash
curl -X POST http://localhost:7070/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Architecture","author":"Robert C. Martin","isbn":"9780134494166","totalCopies":2}'
```

Check out book `1` to member `1`:

```bash
curl -X POST "http://localhost:7070/loans?memberId=1&bookId=1"
```

Return loan `1`:

```bash
curl -X POST http://localhost:7070/loans/1/return
```

List overdue loans:

```bash
curl http://localhost:7070/loans/overdue
```

Reserve and then fulfill a reservation:

```bash
curl -X POST "http://localhost:7070/reservations?memberId=1&bookId=1"
curl -X POST http://localhost:7070/reservations/1/fulfill
```

## Build & tests

```bash
./gradlew build
```

---
&copy; 2026 Diego Urrutia-Astorga, Arquitectura de Software, Universidad Católica del Norte.

## gRPC API - Stage 3

The Library exposes the member module through gRPC using Protocol Buffers.

### Architecture and ports

- REST API: http://localhost:7070
- gRPC server: 127.0.0.1:50051
- Protocol definition: src/main/proto/member.proto
- Java package: cl.ucn.disc.arqsist.library.grpc

Both REST and gRPC reuse the existing MemberService and persistence layer.

### Available RPC operations

| RPC | Request | Response | Description |
|---|---|---|---|
| RegisterMember | RegisterMemberRequest | MemberMessage | Registers a member |
| ListMembers | ListMembersRequest | ListMembersReply | Lists members |
| GetMember | GetMemberRequest | MemberMessage | Finds a member by ID |
| SearchMembers | SearchMembersRequest | ListMembersReply | Searches by name |

### Protocol Buffers messages

- MemberMessage: id, name and email.
- RegisterMemberRequest: name and email.
- ListMembersRequest: empty message.
- ListMembersReply: repeated MemberMessage.
- GetMemberRequest: member ID.
- SearchMembersRequest: name query.

### SearchMembers behavior

- Performs partial name matching.
- Ignores ASCII case differences.
- Trims leading and trailing whitespace.
- Treats '%' and '_' as literal characters.
- Returns results ordered by ascending member ID.
- Returns an empty list when there are no matches.
- Rejects blank queries with INVALID_ARGUMENT.

### Running the server

Start the application from PowerShell:

    .\gradlew.bat run

The application starts the REST API on port 7070
and the gRPC server on port 50051.

### Running the gRPC client

In another PowerShell terminal:

    .\gradlew.bat runClient

To retrieve specific members:

    .\gradlew.bat runClient --args="1 2"

The client uses a five-second deadline.

SearchMembers is available through MemberClient.search(String)
and the generated gRPC stub. The current MemberClientApp
does not expose a search command.

### gRPC error handling

| Condition | gRPC status |
|---|---|
| Successful operation | OK |
| Member not found | NOT_FOUND |
| Invalid argument | INVALID_ARGUMENT |
| Invalid application state | FAILED_PRECONDITION |
| Unexpected server error | INTERNAL |

The laboratory uses plaintext gRPC on the local loopback interface.
No authentication is implemented for the gRPC service.

### Integration tests

Run the member gRPC integration test:

    .\gradlew.bat test --tests MemberGrpcTest

Run the search acceptance tests:

    .\gradlew.bat test --tests MemberSearchTest

Run the full project build:

    .\gradlew.bat clean build

The integration tests use SQLite in memory and
gRPC in-process transport.
