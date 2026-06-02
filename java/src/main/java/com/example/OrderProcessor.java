package com.example;

import java.util.*;
import java.util.concurrent.*;

public class OrderProcessor {

    private static OrderProcessor instance;
    private final Map<String, Object> cache = new HashMap<>();

    // PERF ISSUE: Double-checked locking without volatile
    public static OrderProcessor getInstance() {
        if (instance == null) {
            synchronized (OrderProcessor.class) {
                if (instance == null) {
                    instance = new OrderProcessor();
                }
            }
        }
        return instance;
    }

    // PERF ISSUE: Unbounded cached thread pool
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // PERF ISSUE: Busy wait pattern
    public void waitForResult(Future<String> future) {
        while (!future.isDone()) {
            Thread.yield();
        }
    }

    // PERF ISSUE: Thread.sleep inside synchronized block
    public synchronized void processWithRetry(String orderId) {
        int retries = 3;
        while (retries > 0) {
            try {
                processOrder(orderId);
                return;
            } catch (Exception e) {
                retries--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // PERF ISSUE: Loading all orders without pagination
    public List<Map<String, Object>> getAllOrders() {
        List<Map<String, Object>> orders = new ArrayList<>();
        // SELECT * without LIMIT - loads everything
        // Simulating loading all rows from database
        for (int i = 0; i < 1000000; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", i);
            order.put("amount", Math.random() * 1000);
            order.put("status", "pending");
            orders.add(order);
        }
        return orders;
    }

    // PERF ISSUE: Nested loop O(n^2) for finding duplicates
    public List<String> findDuplicateOrders(List<String> orderIds) {
        List<String> duplicates = new ArrayList<>();
        for (int i = 0; i < orderIds.size(); i++) {
            for (int j = i + 1; j < orderIds.size(); j++) {
                if (orderIds.get(i).equals(orderIds.get(j))) {
                    duplicates.add(orderIds.get(i));
                }
            }
        }
        return duplicates;
    }

    // PERF ISSUE: String concatenation building large SQL
    public String buildBulkInsert(List<Map<String, String>> records) {
        String sql = "INSERT INTO orders (id, name, amount) VALUES ";
        for (int i = 0; i < records.size(); i++) {
            Map<String, String> record = records.get(i);
            sql += "('" + record.get("id") + "', '" + record.get("name") + "', " + record.get("amount") + ")";
            if (i < records.size() - 1) {
                sql += ", ";
            }
        }
        return sql;
    }

    private void processOrder(String orderId) throws Exception {
        // ... processing logic
    }
}
