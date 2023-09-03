SET search_path TO learning;

-- Testing how indexes on expressions work
create table test_expression_index_table
(
    id            bigserial primary key,
    a             text,
    b             text unique,
    c_non_indexed text,
    first_name    text,
    last_name     text
);

-- We know from previous examples that b will have a Unique index to it.
-- If we're going to search filtering by one of a, c_non_indexed, seq. search is going to happen,
-- but if we're going to search by b, then the unique index will be used.


-- Example 1 - create index on LOWER() function for a and compare it to c_non_indexed
------------------------------------------------------------
create index idx_lower_test_expression_index_table
    on test_expression_index_table (lower(a));

-- ouptut: Seq Scan on test_expression_index_table
-- Seq. search is performed
explain
select *
from test_expression_index_table
where lower(c_non_indexed) = 'lower';

-- output: Bitmap Index Scan on idx_lower_test_expression_index_table
--           Index Cond: (lower(a) = 'lower'::text)
-- Expression index created on a with LOWER function was used.
explain
select *
from test_expression_index_table
where lower(a) = 'lower';


-- Example 2 - create index on first_name || ' ' || last_name
------------------------------------------------------------

-- BEFORE CREATING THE INDEX
-- output: Seq Scan on test_expression_index_table
--           Filter: (((first_name || ' '::text) || last_name)
explain
select *
from test_expression_index_table
where first_name || ' ' || last_name = 'example name';

create index idx_compose_test_expression_index_table_first_name_last_name
    on test_expression_index_table ((first_name || ' ' || last_name));

-- output: Bitmap Index Scan on idx_compose_test_expression_index_table_first_name_last_name
-- Index Cond: (((first_name || ' '::text) || last_name) = 'example name'::text)
-- the newly created index was used
explain
select *
from test_expression_index_table
where first_name || ' ' || last_name = 'example name';

-- output: Seq Scan on test_expression_index_table
-- This is still using seq. searching, because the orders of operands in index definition is taken into consideration
explain
select *
from test_expression_index_table
where last_name || ' ' || first_name = 'example name';


-- CONCLUSION: Took a grasp on how expression indexes are used. They are simple to use, just like
-- adding an index on a specific column. When used, we need to take care to the fact that they are
-- not cheap for UPDATES and INSERTS