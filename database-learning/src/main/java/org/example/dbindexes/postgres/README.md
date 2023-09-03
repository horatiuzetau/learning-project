This module is using PostgreSQL, because I did my research on indexes on Postgres.

`docker pull postgres`

`docker run --name learning -e POSTGRES_PASSWORD=pass -d postgres`

First run the SQL command `create schema learning` in order to create the schema we're going to run
examples on.