SET search_path TO learning;

-- Testing how multicolumn indexes work
create table test_multicolumn_index_table
(
    id bigserial PRIMARY KEY,
    a  bigint,
    b  bigint UNIQUE,
    c  text,
    d  bigint,
    e  bigint UNIQUE
);


-- Example 0 - before creating the multi-column index on (a,b)
------------------------------------------------------------
-- output: Index Scan using test_multicolumn_index_table_b_key on test_multicolumn_index_table
--           Index Cond: (b = 6)
--           Filter: (a = 5)
-- This means that the index for b was used (unique index) and a = 5 became a filter.
explain
select *
from test_multicolumn_index_table
where a = 5
  and b = 6;

-- Example 1 - testing multiple situations for a multi-column index on (a,b)
------------------------------------------------------------
-- Create multicolumn index for a, b
create index idx_multi_test_multicolumn_index_table_a_b
    on test_multicolumn_index_table (a, b);

-- a) a = 5 and b = 6
-- output: Index Scan using idx_multi_test_multicolumn_index_table_a_b on test_multicolumn_index_table
--           Index Cond: ((a = 5) AND (b = 6))
-- This means that the index we created was used
explain
select *
from test_multicolumn_index_table
where a = 5
  and b = 6;

-- b) a = 5 or b = 6
-- output: too large to state, but you can see it when running the command
-- It seems that instead of using the multi-column (a,b) index,
-- it used the index-combination feature with:
--   the multi-column index for a = 5
--   the unique index for b = 6
-- then it ORed the results in order to return the result
explain
select *
from test_multicolumn_index_table
where a = 5
  or b = 6;

-- c) a > 5 and b < 6
-- output: Bitmap Index Scan on idx_multi_test_multicolumn_index_table_a_b
--           Index Cond: ((a > 5) AND (b < 6))
-- This time, the query used the multi-column index
explain
select *
from test_multicolumn_index_table
where a > 5
  and b < 6;


-- d) a > 5 or b < 6
-- output: Seq Scan on test_multicolumn_index_table
--         Filter: ((a > 5) OR (b < 6))
-- it didn't use the index at all, but a seq. filtered scan
explain
select *
from test_multicolumn_index_table
where a > 5
   or b < 6;


-- CONCLUSION for Example 1: the query planner is taking choices based on the operators we're using:
--  - when using the OR operator, the query planner often prefers not to use the multi-column index,
--    but instead, it uses separate indexes because it considers them to be faster
--  - when using the AND operator, multi-column index is getting used



-- Example 3 - before creating indexes for (d, e)
------------------------------------------------------------
-- output: Index Scan using test_multicolumn_index_table_e_key on test_multicolumn_index_table
--           Index Cond: (e = 6)
--           Filter: (d = 5)
-- Same thing happened here: because e has a unique index, it was used and d = 5 was treated as a filter
explain
select *
from test_multicolumn_index_table
where d = 5
  and e = 6;


-- Example 4 - Create separate indexes for (d, e)
------------------------------------------------------------
create index idx_test_multicolumn_index_table_d
    on test_multicolumn_index_table (d);

create index idx_test_multicolumn_index_table_e
    on test_multicolumn_index_table (e);

-- a) d = 5 and e = 6
-- output: Index Scan using idx_test_multicolumn_index_table_e on test_multicolumn_index_table
--           Index Cond: (e = 6)
--           Filter: (d = 5)
-- This means that the index for e was used, but d was treated as a filter again,
-- even though we created an index for it
explain
select *
from test_multicolumn_index_table
where d = 5
  and e = 6;

-- b) d = 5 or e = 6
-- output: too large to state, but you can see it when running the command
-- it used the index-combination feature with our both indexes that we created for (d, e)
-- then it ORed the results in order to return the result
explain
select *
from test_multicolumn_index_table
where d = 5
   or e = 6;

-- c) d > 5 and e < 6
-- Index was used for e, but d was used as a filter
explain
select *
from test_multicolumn_index_table
where d > 5
  and e < 6;

-- d) d > 5 or e < 6
-- output: Seq Scan on test_multicolumn_index_table
-- This means that the query planner used seq. searching over index searching.
explain
select *
from test_multicolumn_index_table
where d > 5
   or e < 6;


-- CONCLUSION for Example 4: again, it behaves differently depending on the operator used:
--     if AND is used, the query planner decide on using only one index -
--        seems like the most powerful one: unique index on e
--     if OR is used for less ambiguous operators (=), the query planner uses
--        index-combination feature combining both results at the end
--     if OR is used for ambiguous operators (<, >), the query planner uses seq. read on the table,
--        using respective columns on filtering
-- The columns are more powerful in indexing from left to right at declaration, but in some cases,
-- the unique index is taking over that, because of the power of being found faster
-- (seems like it's doing this for = operations)


-- CONCLUSION: took a grasp of how multi-column B-TREE indexes are treated in query planner's decisions.
-- Also took a grasp on the index-combination feature and found out that it exists and it's being used
-- in most cases because it's faster to run searches on separate indexes then to combine the result