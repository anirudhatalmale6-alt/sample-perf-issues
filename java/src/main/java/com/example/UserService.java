package com.example;

import java.sql.*;
import java.util.*;

public class UserService {

    // PERF ISSUE: Creating new connection per request instead of using connection pool
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/mydb", "root", "password123"
        );
    }

    // PERF ISSUE: String concatenation in loop + N+1 queries
    public String getUserReport(List<Integer> userIds) throws SQLException {
        String report = "";
        Connection conn = getConnection();

        for (Integer id : userIds) {
            // N+1 query pattern - one query per user
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id = " + id);

            if (rs.next()) {
                // String concatenation in loop creates garbage objects
                report += "User: " + rs.getString("name") + "\n";
                report += "Email: " + rs.getString("email") + "\n";
                report += "---\n";
            }

            // PERF ISSUE: Not closing resources properly
        }

        return report;
    }

    // PERF ISSUE: Synchronized entire method when only part needs sync
    public synchronized Map<String, Object> getCachedData(String key) {
        Map<String, Object> cache = new HashMap<>();

        // Expensive operation under lock
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM cache_table WHERE cache_key = '" + key + "'");

            while (rs.next()) {
                cache.put(rs.getString("field"), rs.getObject("value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cache;
    }

    // PERF ISSUE: Unbounded thread creation
    public void processUsersBatch(List<Integer> userIds) {
        for (Integer userId : userIds) {
            new Thread(() -> {
                try {
                    processUser(userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // PERF ISSUE: Autoboxing in tight loop + large object allocation
    public List<Integer> calculateScores(int[] rawScores) {
        List<Integer> results = new ArrayList<>();

        for (int i = 0; i < rawScores.length; i++) {
            // Autoboxing int -> Integer on every iteration
            Integer score = rawScores[i] * 2;
            // Creating new array inside loop for no reason
            int[] temp = new int[1000];
            temp[0] = score;
            results.add(temp[0]);
        }

        return results;
    }

    // PERF ISSUE: Using exceptions for flow control
    public boolean isValidUser(String username) {
        try {
            Integer.parseInt(username);
            return false;
        } catch (NumberFormatException e) {
            // Using exception to determine string is not a number
            return true;
        }
    }

    // PERF ISSUE: Finalizer usage
    @Override
    protected void finalize() throws Throwable {
        System.out.println("Cleaning up UserService");
        super.finalize();
    }

    private void processUser(int userId) throws SQLException {
        Connection conn = getConnection();
        // ... processing
    }
}
