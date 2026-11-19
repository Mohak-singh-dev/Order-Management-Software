# Order Management System (Question 1)

Menu-driven Java JDBC application for the `ecommerce_db.Orders` table.

## Database setup

Run this once in MySQL (the table name and columns follow the exam question):

```sql
CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

CREATE TABLE IF NOT EXISTS Orders (
    order_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    product_name VARCHAR(100),
    quantity INT,
    unit_price DOUBLE,
    status VARCHAR(30)
);
```

## Run

Set the connection details for your local MySQL instance. The defaults are URL
`jdbc:mysql://localhost:3306/ecommerce_db`, user `root`, and an empty password.
Override them with environment variables if needed:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/ecommerce_db'
$env:DB_USER = 'root'
$env:DB_PASSWORD = 'your-password'
```

Then run `org.example.Main` from IntelliJ or use `mvn compile` followed by your
preferred Java run command. The MySQL JDBC driver is managed by Maven.

## Menu operations

1. Insert three sample orders with `PreparedStatement`; duplicate IDs are
   reported and skipped.
2. List orders matching the entered status with quantity above two, sorted by
   unit price descending.
3. Add a positive amount to an existing order's quantity.
4. Change an existing order to Pending, Shipped, or Delivered and print its
   updated details.
5. Print `SUM(quantity * unit_price)` across all orders.
6. Delete an order only when its status is Cancelled.
