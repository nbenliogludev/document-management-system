# Document Search Optimization (EXPLAIN)

This document describes how the document search in the system is optimized using correct PostgreSQL indexes, and demonstrates the query execution plan.

## 1. Search Scenario

An example of a real user request or an asynchronous worker task: **Retrieve the latest documents with the `SUBMITTED` status**.

The SQL query looks like this:
```sql
SELECT *
FROM documents
WHERE status = 'SUBMITTED'
ORDER BY created_at DESC
LIMIT 50;
```

## 2. EXPLAIN ANALYZE

When executing this query using `EXPLAIN ANALYZE`, the PostgreSQL query plan demonstrates the optimal execution path:

```sql
EXPLAIN ANALYZE
SELECT *
FROM documents
WHERE status = 'SUBMITTED'
ORDER BY created_at DESC
LIMIT 50;
```

**Expected Result (fragment):**
```text
Limit  (cost=0.14..5.91 rows=50 width=227) (actual time=0.012..0.045 rows=50 loops=1)
  ->  Index Scan using idx_documents_status_created_at on documents  (cost=0.14..5.91 rows=50 width=227)
        Index Cond: ((status)::text = 'SUBMITTED'::text)
```
*(Note: PostgreSQL uses an `Index Scan` rather than a full table `Seq Scan`)*

## 3. Why This Index Is Used

A special composite B-Tree index has been created in the system (via Liquibase):
`idx_documents_status_created_at (status, created_at DESC)`

This index allows the query optimizer to do several things simultaneously:
- **Filter by `status`**: The database instantly positions itself on the first suitable element in the tree, bypassing all other statuses.
- **Instantly return rows in the required sort order**: Because the records inside the index are already ordered by descending time (`created_at DESC`), the DB simply reads the elements in order.
- **Avoid in-memory Sort**: No memory buffers are allocated on the server for sorting, as the data is already prepared.
- **Avoid Seq Scan**: There is no full table scan, saving CPU cycles and disk I/O.

## 4. What This Gives the System

**Without the index:**
- `Seq Scan` (Reading all table pages).
- A separate, heavy `Sort` step for all filtered rows.
- Severe performance degradation and increased Latency as the volume of data in the table grows.

**With the index:**
- A targeted `Index Scan`.
- Ultrafast and predictable pagination even with large offsets.
- Scalable bulk processing (batch approve / submit), since workers grab their chunks of tasks (LIMIT M) in milliseconds without blocking other consumers with long reads.

## 5. Infrastructure and Deployment

- The creation of indexes is guaranteed at the database schema level: they are automatically applied and managed by the **Liquibase** configuration (`007-optimize-document-search-indexes.yaml`).
- The **PostgreSQL** query planner optimizer picks up this index automatically as long as the statistics are up-to-date, so the system operates without complex manual tuning or forced hints.
