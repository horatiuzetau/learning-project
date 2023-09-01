package org.example.dbconnectionpool;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class DBConnectionPoolService {

    final int poolSize;
    final List<String> queryList;
    static final int GET_CONNECTION_MAXIMUM_RETRIES_COUNT = 3;
    static final int GET_CONNECTION_WAIT_TIME_MILLI = 500;
    static final int KILL_CONNECTION_MAXIMUM_RETRIES_COUNT = 3;
    static final int KILL_CONNECTION_WAIT_TIME_MILLI = 500;

    static Queue<DbConnection> connections = new LinkedList<>();

    public DBConnectionPoolService(int poolSize, List<String> queryList) {
        if (poolSize < 1) {
            throw new RuntimeException("Pool size should be at least 1");
        }
        this.poolSize = poolSize;
        this.queryList = queryList;
    }

    public void runConenectionPoolTest() throws InterruptedException {
        try {
            initializeConnections();
            // Create a list of queries to be executed
            // INSERT queries will call the generateRandomRows method - this was made this way in order to have slower queries

            // Create threads based on the defined queries
            var threads = queryList.stream().map(query -> new Thread(() -> {
                try {
                    executeQuery(query);
                } catch (SQLException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            })).toList();

            // Start created threads
            for (var thread : threads) {
                thread.start();
            }

            // Join threads in order to wait for everything to finish, in order to also safely kill DB connections
            threads.forEach(thread -> {
                try {
                    log("Joining thread " + thread.getName());
                    thread.join();
                } catch (InterruptedException e) {
                    thread.interrupt();
                }
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            log("Killing connections...");
            killConnections();
        }
    }

    public void initializeConnections() throws SQLException {
        final String host = "localhost";
        final String port = "3306";
        final String dbName = "learning";
        final String username = "root";
        final String password = "pass";
        // Initialize number of connection equal to the defined poolSize
        for (int i = 0; i < poolSize; i++) {
            var connection = new DbConnection(i, host, port, dbName, username, password);
            connection.setConnection(DriverManager.getConnection(
                    connection.getURL(), connection.getUsername(), connection.getPassword()
            ));
            connections.add(connection);
            log("Connection " + connection.getId() + " was created!");
        }
        // Empty line in logs
        log("----");
    }

    public synchronized void killConnections() throws InterruptedException {
        var availableRetries = KILL_CONNECTION_MAXIMUM_RETRIES_COUNT;
        // Wait for connections to finalize before closing them
        while (connections.size() != poolSize) {
            this.wait(KILL_CONNECTION_WAIT_TIME_MILLI);
            if (availableRetries-- == 0) {
                // if they are not finalized, log how many connections are missing
                log("Connections missing: " + (poolSize - connections.size())
                        + "poolSize= " + poolSize + ";connectionsSize=" + connections.size());
            }
        }

        // Foreach available connections in connections queue, close them
        connections.forEach(connection -> {
            try {
                connection.getConnection().close();
                log("Connection " + connection.getId() + " was closed!");
            } catch (SQLException e) {
                log("Connection " + connection.getId() + " couldn't be closed!");
                throw new RuntimeException(e);
            }
        });
    }

    private static synchronized DbConnection getConnection() throws InterruptedException {
        var maximumTries = GET_CONNECTION_MAXIMUM_RETRIES_COUNT;
        // Trying to get connection
        var connection = connections.poll();
        // If there is no connection available
        if (connection == null) {
            // While there is no connection available
            while (connections.isEmpty()) {
                log("Connection pool is empty! Waiting for free connection | ATTEMPTS LEFT " + maximumTries);
                // Wait for an available connection
                Thread.sleep(GET_CONNECTION_WAIT_TIME_MILLI);
                // If waiting too long, throw an exception
                if (maximumTries-- == 0) {
                    var errorMessage = "Couldn't get connection";
                    log(errorMessage);
                    throw new RuntimeException(errorMessage);
                }
            }
            // After waiting and getting a connection, get available connection
            return connections.remove();
        }
        // If there is a connection available, return it
        return connection;
    }

    public static void freeConnection(DbConnection dbConnection) {
        // Put connection back to connections list
        connections.add(dbConnection);
    }

    public static List<String> executeQuery(String sqlCommand) throws SQLException, InterruptedException {
        // Get an available connection
        var availableConnection = getConnection();

        // Log connection in order to see on which thread and connection we're running the query
        log("Initialized connection " + availableConnection.getId() + " | QUERY: " + sqlCommand);
        var resultList = new ArrayList<String>();

        // If sqlCommand contains INSERT, call for generateRandomRows
        if (sqlCommand.contains("INSERT")) {
            var sqlCommandWords = sqlCommand.split(" ");
            var tableName = sqlCommandWords[1];
            var numberOfRows = Integer.valueOf(sqlCommandWords[2]);

            generateRandomRows(availableConnection, tableName, numberOfRows);
            log("Inserted " + numberOfRows + " number of rows");

            freeConnection(availableConnection);
            return resultList;
        }

        // For SELECT statements
        if (sqlCommand.contains("SELECT")) {
            try (var statement = availableConnection.getConnection().prepareStatement(sqlCommand)) {
                // Execute statement
                statement.executeQuery();
                var resultSet = statement.getResultSet();

                // Get request's result
                while (resultSet.next()) {
                    resultList.add((String) resultSet.getObject("name"));
                }

                log("Statement was executed! | QUERY: " + sqlCommand);
                freeConnection(availableConnection);
                // Return result
                return resultList;
            } catch (SQLException e) {
                log("[ERROR] Statement couldn't be executed! | QUERY: " + sqlCommand);
                freeConnection(availableConnection);
                throw new RuntimeException(e);
            }
        }

        freeConnection(availableConnection);
        return resultList;
    }

    private static void generateRandomRows(DbConnection dbConnection, String tableName, int rows) throws SQLException {
        // Basic functionality for inserting random values into a table
        var sqlCommand = String.format("INSERT INTO %s (`name`) VALUES (?)", tableName);
        for (int i = 0; i < rows; i++) {
            try (var statement = dbConnection.getConnection().prepareStatement(sqlCommand)) {
                statement.setString(1, RandomStringUtils.randomAlphabetic(6));
                statement.executeUpdate();
            }
        }
    }

    private static void log(String logMessage) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + logMessage);
    }

}
