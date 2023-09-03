-- Testing how hash indexes work
create table test_index_table
(
    id             bigserial PRIMARY KEY,
    uq             bigint UNIQUE,
    uq_indexed     bigint UNIQUE,
    non_uq         bigint,
    non_uq_indexed bigint
);

-- Example 1 - using hash index on ID (PRIMARY KEY) column
------------------------------------------------------------
-- The documentation states that unique indexes can only be created as B-TREES

-- I thought it's a good idea to have it on the ID and I tried doing it:
create index idx_hash_test_index_table_id
    on test_index_table using hash (id);

-- output: "Index Scan using idx_hash_test_index_table_id on test_index_table"
-- This means that the idx_hash_test_index_table_id index is indeed used and that findById will run in O(1)
explain
select *
from test_index_table
where id = 5;


-- If using the id with operators like > or <, then the PK index is used
-- output: "Bitmap Index Scan on test_index_table_pkey"
explain
select *
from test_index_table
where id >= 5;


-- Example 2 - using hash index on a unique column
------------------------------------------------------------
create index idx_hash_test_index_table_uq_indexed
    on test_index_table using hash (uq_indexed);

-- output: "Index Scan using idx_hash_test_index_table_uq on test_index_table"
-- This means that also in this case, the hash index we created is getting used
explain
select *
from test_index_table
where uq_indexed = 1;


-- output: "Bitmap Index Scan on test_index_table_uq_indexed_key"
-- This means that the unique index is used here, but if we're using equality, the hash index is used.
explain
select *
from test_index_table
where uq_indexed > 1;


-- Example 3 - using hash index on a non-unique column
------------------------------------------------------------
create index idx_hash_test_index_table_non_uq_indexed
    on test_index_table using hash (non_uq_indexed);

-- output: "Bitmap Index Scan on idx_hash_test_index_table_non_uq_indexed"
-- This means that for this NON UNIQUE column, the hash index was used successfully.
explain
select *
from test_index_table
where non_uq_indexed = 5;

-- output: "Seq Scan on test_index_table"
-- No index is used, because we don't have any defined.
explain
select *
from test_index_table
where non_uq_indexed > 1;


-- Example 4: Using uq not indexed
------------------------------------------------------------
-- output: Index Scan using test_index_table_uq_key on test_index_table
-- NOTE! Also here we can't see Bitmap keyword, but we can be pretty sure that the uq index is used.
explain
select *
from test_index_table
where uq = 5;


-- Example 5: Using non_uq not indexed
------------------------------------------------------------
-- output: Seq Scan on test_index_table
-- Meaning it's not using any index
explain
select *
from test_index_table
where non_uq = 5;


-- CONCLUSION:
-- PRO: it looks like it's a good idea to store a hash index on PRIMARY KEYS,
-- because most of the time we're looking for single entities by ID and O(1) is the best we can get;
-- CON: though it's saving us time when fetching for an ID, the index has to always be updated when
-- a new entry is added - I don't know if it's worth the effort, even though inserting in a
-- hash index shouldn't be that expensive

-- QUESTION 2: Why are the outputs for hash index different in examples 1-2 from example 3?
-- The documentation(https://www.postgresql.org/docs/current/indexes-unique.html) states that:
-- Currently, only B-tree indexes can be declared unique. - first time, this made me think that
-- hash indexes will not work on UNIQUE indexed columns, but it seems like it's only that we can't
-- directly create a unique index using HASH strategy.
--
-- Is this true?

-- QUESTION 2: What do you think? is HASH-typed index worth using for PRIMARY KEY columns?

