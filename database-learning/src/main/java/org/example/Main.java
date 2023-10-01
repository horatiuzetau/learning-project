package org.example;

import org.example.dbconnectionpool.DBConnectionPoolService;

import java.util.List;
import org.example.functionalinterfaces.FunctionalInterfaceService;
import org.example.reflection.ReflectionTestService;

public class Main {

    public static void main(String[] args) {
//        testDBConnectionPool();
//        testFunctionalInterface();
        testReflection();
    }

    public static void testReflection() {
        var reflectionTestService = new ReflectionTestService();
        reflectionTestService.testReflection();
    }

    public static void testFunctionalInterface() {
        var functionalInterfaceService = new FunctionalInterfaceService();
        functionalInterfaceService.testFunctionalInterface();
    }

    public static void testDBConnectionPool() {
        try {
            var queryList = List.of(
                    String.format("SELECT * FROM %s", "test_data_read"),
                    String.format("INSERT %s %s", "test_data_write", "500"),
                    String.format("SELECT id, UPPER(`name`) as name FROM %s", "test_data_read"),
                    String.format("INSERT %s %s", "test_data_write", "200"),
                    String.format("SELECT '2' as name FROM %s", "test_data_read"),
                    String.format("SELECT '1' as name FROM %s", "test_data_read")
            );

            var dbConnectionPoolService = new DBConnectionPoolService(1, queryList);
            dbConnectionPoolService.runConenectionPoolTest();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Something went wrong with the DB Connection pool testing");
        }
    }
}