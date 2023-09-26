# SETUP

You will need a Docker container holding running a MariaDB image. Run these commands in order to create one:

`docker pull mariadb`

`docker run --name maria-learning -e MYSQL_ROOT_PASSWORD=pass -p 3306:3306 -d mariadb`

_**Connection data:**_

`host = localhost`

`port = 3306`

`username = root`

`password = pass`

Now we have an instance of mariadb running.
From now on, the following command will start the container everytime you restart your computer:

`docker start maria-learning`

_Run the start.sql script on the new database in order to create two tables: test_data_read & test_data_write,
both having the same structure: (id <numeric>, name <string>)_


# IMPLEMENTATION

All details can be understood from the code and explanatory comments.


# TESTING

The Main.java contains a simple testing structure.
We should initialize the DBConnectionPoolService giving the poolSize and the queryList. The queryList contains a list of
SQL commands that will run asynchronous.

The structure of these queries should respect the following rules(these were enough to cover the purpose of DB pooling testing) :
* we can have SELECT statements (e.g. `SELECT * FROM test_data_read`)
* we can have INSERT statements with the structure `INSERT <table_name> <number_of_rows>`
        -> this will insert <number_of_rows> rows with  mock data inside <table_name>

    ! NOTE that these statements should apply on one of the existing tables: test_data_read / test_data_write

We also have some static variables which can be modified in order to adjust the pool behavior:
* _GET_CONNECTION_MAXIMUM_RETRIES_COUNT_ - number of retries until program stops trying
        to find an available connection;
* _GET_CONNECTION_WAIT_TIME_MILLI_ - time in milli seconds the program waits until next try
        to get an available connection
* _KILL_CONNECTION_MAXIMUM_RETRIES_COUNT_ - number of retries until program stops trying
        to wait for all connections before killing them
* _KILL_CONNECTION_WAIT_TIME_MILLI_ - time in milli seconds the program is waiting until next try
        when waiting for all connections before killing them
