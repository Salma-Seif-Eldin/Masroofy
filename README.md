# Masroofy — Personal Budgeting App

A desktop personal budgeting application built in Java (Swing + SQLite), developed as a full software engineering project at Cairo University covering both system design and implementation.

The project started from a Software Requirements Specification (SRS) written by another team — simulating a real client handoff — and required producing a complete Software Design Specification (SDS) before writing a single line of code.

## Demo & Media

📁 [Demo video, SDS document, UI screenshots & diagrams (Google Drive)](

)

📄 [JavaDoc Documentation](_https://github.com/dimadel660/Masroofy/tree/main/javadoc)

## What It Does

Masroofy lets users manage their personal finances across budget cycles. Core features include:

- Sign up and authenticate with a 4-digit PIN
- Create and manage budget cycles with spending limits
- Add, categorize, and track expenses
- View a dashboard with spending summaries
- Receive alerts when approaching or exceeding budget limits
- Data persists across sessions via a local SQLite database

## Architecture

The project is structured using the **MVC pattern** to enforce separation of concerns:

- **Models** — data structures and business logic (`BudgetCycle`, `Expense`, `Category`)
- **Views** — UI components built with Java Swing (`DashboardActivity`, `AuthActivity`)
- **Controllers** — coordinate between UI and data layer (`BudgetManager`, `ExpenseController`, `AlertManager`)
- **Database (DAO)** — Data Access Objects handle all SQLite interactions, keeping SQL logic out of business logic (`TransactionDAO`, `CycleDAO`)

## Design

The full Software Design Specification (SDS) includes:

- System architecture diagram
- Full class diagram with attributes, operations, and relationships
- Seven detailed sequence diagrams with a class-sequence traceability table
- State diagram
- SOLID principles and design patterns applied

### SOLID Principles Applied

- **Interface Segregation (ISP)** — interfaces are split into focused, specific contracts rather than large general ones
- **Open/Closed (OCP)** — new reporting styles or categories can be added without modifying core spending logic
- **Dependency Inversion (DIP)** — high-level modules like `ExpenseController` depend on the `BudgetManager` abstraction rather than hardcoded instances

## Getting Started

**Requirements:**
- Java Development Kit (JDK) 8 or higher
- SQLite JDBC Driver

**Setup:**
1. Add the SQLite JDBC jar to your project's classpath.
2. Run the main application entry point.
3. The app automatically initializes the database file (`Masroofy.db`) on first run via `DatabaseManager.createTables()`.
4. Sign up with a new 4-digit PIN and start your first budget cycle.

## Built With

- Java (Swing)
- SQLite + JDBC
- MVC Architecture
- JavaDoc for documentation

