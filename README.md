# Sample Performance Issues Repository

This repository contains intentional performance anti-patterns for testing the Performance Review Agent.

## Files

### Java
- `UserService.java` - Connection per request, N+1 queries, string concat in loops, synchronized methods, thread spawning, autoboxing, finalizers
- `OrderProcessor.java` - Double-checked locking, unbounded thread pools, busy waits, Thread.sleep in sync blocks, O(n^2) algorithms

### Python
- `data_processor.py` - Nested loops, sync I/O in async, file memory loading, string concat, no caching, connection per request

### SQL
- `bad_queries.sql` - SELECT *, function on indexed columns, leading wildcards, correlated subqueries, cartesian joins, cursor processing

### Frontend
- `Dashboard.tsx` - Memory leaks, missing cleanup, DOM thrashing, inline functions, no memoization, no virtualization

## Purpose

Push commits to this repo to trigger the Performance Review Agent webhook. The agent will analyze the diffs and post findings to the linked Jira ticket.

Include a Jira ticket key in your commit message (e.g., `PERF-123: Add user service`) to have the review posted as a Jira comment.
