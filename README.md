The project is structured using the **MVC Pattern** to ensure a separation of concerns:

- **Models**: Represent the data structures and business logic (e.g., `BudgetCycle`, `Expense`, `Category`).
- **Views**: Handle the UI components and user interactions using Java Swing (e.g., `DashboardActivity`, `AuthActivity`).
- **Controllers**: Manage the application flow and coordinate between the UI and the data layer (e.g., `BudgetManager`, `ExpenseController`, `AlertManager`).
- **Database (DAO)**: Data Access Objects manage SQLite interactions, keeping SQL logic separate from the business logic (e.g., `TransactionDAO`, `CycleDAO`).

##  SOLID Principles Applied

1. **Interface Segregation Principle (ISP)**: ENo client should be forced to depend on methods it does not use. Large interfaces should be split into smaller, more specific ones.
2. **Open/Closed Principle (OCP)**: The system is designed to be extendable. New reporting styles or categories can be added without modifying the core spending logic.
3. **Dependency Inversion Principle (DIP)**: High-level modules like `ExpenseController` depend on the `BudgetManager` abstraction rather than hardcoded instances.

##  Requirements

- Java Development Kit (JDK) 8 or higher.
- SQLite JDBC Driver.

##  Setup and Installation

1. Ensure the SQLite JDBC jar is added to your project's classpath.
2. Run the main application entry point (The project uses `DatabaseManager.createTables()` to initialize the database file `Masroofy.db` automatically).
3. Sign up with a new 4-digit PIN and start your first budget cycle!.