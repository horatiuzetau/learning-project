package org.example.dbconnectionpool;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Connection;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DbConnection {

    int id;
    String host;
    String port;
    String dbName;
    String username;
    String password;
    Connection connection;

    public DbConnection(int id, String host, String port, String dbName, String username, String password) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.username = username;
        this.password = password;
    }

    public String getURL() {
        return String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
    }
}
