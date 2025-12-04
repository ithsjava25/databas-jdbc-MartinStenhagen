package com.example;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.Arrays;

public class Main {

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo: Starting point for your code
        String username;
        boolean loggedIn = false;
        char[] passwordChars; //char-array för ökad säkerhet med lösenord (String är immutable).
        IO.println("Welcome to the Moon Mission Project.\n");


        while (!loggedIn) {
            username = IO.readln("Enter username: ");
            passwordChars = IO.readln("Enter password: ").toCharArray();
            if (isLoginValid(jdbcUrl, dbUser, dbPass, username, passwordChars)) {
                IO.println("Logged in successfully as " + username);
                loggedIn = true;

            } else {
                String input = IO.readln("Invalid username or password. Enter 0 to exit or any other key to try again: ");
                if (input.equals("0")) {
                    System.exit(0);
                }
            }

        }


        while (true) {
            IO.println("""
                    \n-----------------------------
                    1) List moon missions
                    2) Get moon mission by id
                    3) Count missions by year
                    4) Create an account
                    5) Update password
                    6) Delete account
                    0) Exit
                    -----------------------------
                    """);

            String input = IO.readln("Enter choice: ").trim();
            if (input.isEmpty() || !isInputValid(input, 0, 6)) {
                IO.println("Invalid input, enter a number 0-6.\n");
                continue;
            }
            int choice = Integer.parseInt(input);
            switch (choice) {
                case 0 -> {
                    IO.println("Exiting program...");
                    return;
                }

                case 1 -> listMissions(jdbcUrl, dbUser, dbPass);

                case 2 -> getMissionById(jdbcUrl, dbUser, dbPass);

                case 3 -> countMissionsByYear(jdbcUrl, dbUser, dbPass);

                case 4 -> createAccount(jdbcUrl, dbUser, dbPass);

                case 5 -> updatePassword(jdbcUrl, dbUser, dbPass);

                case 6 -> deleteAccount(jdbcUrl, dbUser, dbPass);
            }
        }


    }

    private boolean isLoginValid(String jdbcUrl, String dbUser, String dbPass, String username, char[] password) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            String query = "select * from account where name = ? and password = ? ";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, new String(password));
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Hjälpmetod istället för att ha ett default-case för att fånga icke-numerisk input innan switch körs.
    private boolean isInputValid(String input, int min, int max) {
        try {
            int n = Integer.parseInt(input);
            return (n >= min && n <= max);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void listMissions(String jdbcUrl, String dbUser, String dbPass) {
        String query = "select spacecraft from moon_mission";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            IO.println("\n--- Moon Mission Spacecrafts ---\n");
            while (result.next()) {
                String spacecraft = result.getString("spacecraft");
                IO.println(spacecraft);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getMissionById(String jdbcUrl, String dbUser, String dbPass) {
        String input = IO.readln("Enter mission id: ").trim();
        int id;

        try {
            id = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            IO.println("Invalid input. Mission id must be a number.\n");
            return;
        }

        String query = "select * from moon_mission where mission_id = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement statement = connection.prepareStatement(query)) {
            ;
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    IO.println("No mission found with id: " + id + "\n");
                    return;
                }
                IO.println("\n--- Mission Details ---\n");
                ResultSetMetaData metadata = result.getMetaData();
                int columnCount = metadata.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String column = metadata.getColumnLabel(i);
                    String value = result.getString(i);
                    IO.println(String.format("%-15s : %s", column, value));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void countMissionsByYear(String jdbcUrl, String dbUser, String dbPass) {
        String input = IO.readln("Enter mission year: ").trim();
        int year = 0;

        try {
            year = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            IO.println("Invalid input. Mission year must be a number.\n");
        }
        String query = "select count(*) as numberOfMissions from moon_mission where year(launch_date) = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, year);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int numberOfMissions = result.getInt("numberOfMissions");
                    if (numberOfMissions == 1) {
                        IO.println("Found " + numberOfMissions + " mission for year: " + year + "\n");
                    } else if (numberOfMissions > 1) {
                        IO.println("Found " + numberOfMissions + " missions for year: " + year + "\n");
                    } else {
                        IO.println("No missions found for year: " + year + "\n");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAccount(String jdbcUrl, String dbUser, String dbPass) {
    }

    private void updatePassword(String jdbcUrl, String dbUser, String dbPass) {
    }

    private void deleteAccount(String jdbcUrl, String dbUser, String dbPass) {
    }


    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
