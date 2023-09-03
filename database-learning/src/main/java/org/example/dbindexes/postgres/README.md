This module is using PostgreSQL, because I did my research on indexes on Postgres.

`docker pull postgres`

`docker run --name learning -e POSTGRES_PASSWORD=pass -d postgres`

First run the SQL command `create schema learning` in order to create the schema we're going to run
examples on.

The order the SQL files are numbered defines the order I implemented them. I also studied about
Partial Indexes, but I didn't think it's necessary to exemplify them, because they are less often
used and I don't see applying their principles in the near future.

Also, learned from experience that Postgres is not creating indexes for Foreign Key columns - they
are needed for a better performance and have to be created manually - standard B-TREE indexes are
enough.