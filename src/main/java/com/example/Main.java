package com.example;

import com.example.model.Account;
import com.example.model.MoonMission;
import com.example.repo.AccountRepository;
import com.example.repo.MoonMissionRepository;
import com.example.repo.jdbc.JdbcAccountRepository;
import com.example.repo.jdbc.JdbcMoonMissionRepository;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.util.ISO8601Utils;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private Scanner scanner;
    private AccountRepository accountRepo;
    private MoonMissionRepository missionRepo;

    static void main(String[] args) throws SQLException {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() throws SQLException {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        accountRepo = new JdbcAccountRepository(dataSource);
        missionRepo = new JdbcMoonMissionRepository(dataSource);
        scanner = new Scanner(System.in);
        login();
        runMenu(accountRepo,missionRepo);
    }

    private void runMenu(AccountRepository accountRepo, MoonMissionRepository missionRepo) throws SQLException {
        while (true) {
            System.out.println("""
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

            System.out.println("Enter choice: ");
            String input = scanner.nextLine();
            if (input.isEmpty() || !isInputValid(input, 0, 6)) {
                System.out.println("Invalid input, enter a number 0-6.\n");
                continue;
            }
            int choice = Integer.parseInt(input);
            switch (choice) {
                case 0 -> {
                    IO.println("Exiting program...");
                    return;
                }

                case 1 -> listMissions(missionRepo);

                case 2 -> getMissionById(missionRepo);

                case 3 -> countMissionsByYear(missionRepo);

                case 4 -> createAccount(accountRepo);

                case 5 -> updatePassword(accountRepo);

                case 6 -> deleteAccount(accountRepo);
            }
        }
    }


    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        if (username == null) return;

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (password == null) return;

        boolean valid = accountRepo.findByUsernameAndPassword(username,password).isPresent();
        if(!valid) {
            System.out.println("Invalid username or password");
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

    private void listMissions(MoonMissionRepository missionRepo) {
        List<MoonMission> missions = missionRepo.findAll();
        if (missions.isEmpty()) {
            System.out.println("No missions found.");
        }
        for (MoonMission mission : missions){
        System.out.println(mission.getSpacecraft());
        }
    }

    private void getMissionById(MoonMissionRepository missionRepo) {
        System.out.println("Enter mission id: ");
        String input = scanner.nextLine();
        int id;

        try {
            id = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Mission id must be a number.\n");
            return;
        }
        Optional<MoonMission> mission = missionRepo.findById(id);

        if (mission.isEmpty()) {
            System.out.println("No mission found with id: " + id + "\n");
            return;
        }
        System.out.println("\n---Mission Details---\n");
        System.out.println("Mission id: " + mission.get().getId());
        System.out.println("Spacecraft: " + mission.get().getSpacecraft());
        System.out.println("Launch_date: " + mission.get().getLaunchDate());
        System.out.println("Carrier rocket: " + mission.get().getCarrier_rocket());
        System.out.println("Operator: " + mission.get().getOperator());
        System.out.println("Mission type: " + mission.get().getMission_type());
        System.out.println("Outcome: " + mission.get().getOutcome());


    }

    private void countMissionsByYear(MoonMissionRepository missionRepo) {
        System.out.println("Enter mission year: ");
        String input = scanner.nextLine();
        int year;
        try{
            year = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Mission year must be a number.\n");
            return;
        }
        int numberOfMissions = missionRepo.countByYear(year);
        if (numberOfMissions == 0) {
            System.out.println("No missions found for year: " + year + "\n");
        } else if (numberOfMissions == 1) {
            System.out.println("Found " + numberOfMissions + " mission for year: " + year + "\n");
        }else{
            System.out.println("Found " + numberOfMissions + " missions for year: " + year + "\n");
        }
    }

    private void createAccount(AccountRepository accountRepo) {
        System.out.println("Enter a first name: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter a last name: ");
        String lastName = scanner.nextLine();
        System.out.println("Enter ssn: ");
        String ssn = scanner.nextLine();
        System.out.println("Choose a password: ");
        String password = scanner.nextLine();

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            System.out.println("Invalid input. Names and password cannot be empty.\n");
            return;
        }

        if (accountRepo.existsBySsn(ssn)) {
            System.out.println("An account with ssn: " + ssn + " already exists.\n");
            return;
        }

        String name = (firstName.length() >= 3 ? firstName.substring(0, 3) : firstName) +
                (lastName.length() >= 3 ? lastName.substring(0, 3) : lastName);

        Account account = new Account(name, password, firstName, lastName, ssn);
        accountRepo.create(account);
        System.out.println("Created account with username: " + name + "\n");
    }


    private void updatePassword(AccountRepository accountRepo) {
        System.out.println("Enter user id: ");
        String input = scanner.nextLine();
        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. user id must be a number.\n");
            return;
        }

        if(!accountRepo.existsByUserId(userId)){
            System.out.println("No user with id: " + userId + "\n");
            return;
        }

        System.out.println("Enter a new password: ");
        String newPassword = scanner.nextLine();

        accountRepo.updatePassword(userId, newPassword);
        System.out.println("Updated password for user with id: " + userId + "\n");

        String checkQuery = "select * from account where user_id = ?";
        String updateQuery = "update account set password = ? where user_id = ?";
    }


    private void deleteAccount(Connection connection) throws SQLException {
        String fullName;
        System.out.println("Enter user id to delete: ");
        String input = scanner.nextLine();
        int userId = 0;
        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. user id must be a number.\n");
            return;
        }
        String checkQuery = "select * from account where user_id = ?";
        String deleteQuery = "delete from account where user_id = ?";


        try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
            checkStatement.setInt(1, userId);
            try (ResultSet result = checkStatement.executeQuery()) {
                if (!result.next()) {
                    System.out.println("No user found with id: " + userId + "\n");
                    return;
                }
                fullName = result.getString("first_name") + " " + result.getString("last_name");
            }
        }
//        System.out.println("Delete account for " + fullName + " ? (yes/no): ");
//        String confirm = scanner.nextLine();
//        if (!confirm.equals("yes")) {
//            System.out.println("Deletion cancelled.\n");
//            return;
//        }

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
            deleteStatement.setInt(1, userId);

            int rows = deleteStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Account with id: " + userId + " has been deleted\n");
            } else {
                System.out.println("Failed to delete account.\n");

            }
        }


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

