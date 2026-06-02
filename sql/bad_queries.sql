-- PERF ISSUE: SELECT * fetches unnecessary columns, prevents covering indexes
SELECT * FROM orders WHERE status = 'pending';

-- PERF ISSUE: Function on indexed column prevents index usage
SELECT * FROM users WHERE UPPER(email) = 'JOHN@EXAMPLE.COM';

-- PERF ISSUE: LIKE with leading wildcard - cannot use index
SELECT * FROM products WHERE name LIKE '%widget%';

-- PERF ISSUE: Correlated subquery executes for each row
SELECT o.id, o.amount,
    (SELECT COUNT(*) FROM order_items oi WHERE oi.order_id = o.id) as item_count
FROM orders o
WHERE o.status = 'active';

-- PERF ISSUE: Implicit cartesian join (missing JOIN condition)
SELECT u.name, o.amount
FROM users u, orders o
WHERE o.status = 'pending';

-- PERF ISSUE: OR on different columns prevents index merge
SELECT * FROM orders
WHERE customer_id = 123 OR shipping_address LIKE '%New York%';

-- PERF ISSUE: NOT IN with subquery - use NOT EXISTS or LEFT JOIN instead
SELECT * FROM users
WHERE id NOT IN (SELECT user_id FROM orders WHERE created_at > '2024-01-01');

-- PERF ISSUE: UPDATE without WHERE clause
UPDATE products SET price = price * 1.1;

-- PERF ISSUE: Cursor-based row-by-row processing instead of set-based
-- PL/SQL anti-pattern
DECLARE
    CURSOR c_orders IS SELECT id, amount FROM orders WHERE status = 'pending';
    v_total NUMBER := 0;
BEGIN
    FOR rec IN c_orders LOOP
        UPDATE orders SET processed = 1 WHERE id = rec.id;
        v_total := v_total + rec.amount;
    END LOOP;
    COMMIT;
END;

-- PERF ISSUE: Missing index hint for large table scan
SELECT customer_id, SUM(amount) as total
FROM orders
WHERE created_at BETWEEN '2024-01-01' AND '2024-12-31'
GROUP BY customer_id
HAVING SUM(amount) > 10000
ORDER BY total DESC;

-- PERF ISSUE: Nested subqueries that could be JOINs
SELECT * FROM users
WHERE id IN (
    SELECT user_id FROM orders
    WHERE product_id IN (
        SELECT id FROM products
        WHERE category_id IN (
            SELECT id FROM categories WHERE name = 'Electronics'
        )
    )
);
