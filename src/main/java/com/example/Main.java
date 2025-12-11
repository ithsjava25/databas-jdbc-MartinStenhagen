package com.example;

import com.example.model.Account;
import com.example.model.MoonMission;
import com.example.repo.AccountRepository;
import com.example.repo.MoonMissionRepository;
import com.example.repo.jdbc.JdbcAccountRepository;
import com.example.repo.jdbc.JdbcMoonMissionRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private Scanner scanner;
    private AccountRepository accountRepo;
    private MoonMissionRepository missionRepo;

    public static void main(String[] args) {
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

        DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        accountRepo = new JdbcAccountRepository(dataSource);
        missionRepo = new JdbcMoonMissionRepository(dataSource);
        scanner = new Scanner(System.in);
        try {
            login();
            runMenu();
        } catch (DatabaseException dbEx) {
            System.out.println("A database error occurred during login. Please try again later.");

        }
    }

    private void runMenu() {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.println("Enter choice: ");

            try {
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> listMissions();
                    case "2" -> getMissionById();
                    case "3" -> countMissionsByYear();
                    case "4" -> createAccount();
                    case "5" -> updatePassword();
                    case "6" -> deleteAccount();
                    case "0" -> {
                        System.out.println("Exiting program...");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (DatabaseException dbEx) {
                System.out.println("A database error occurred. Please try again.\n");
            } catch (Exception e) {
                System.out.println("An unexpected error occurred. Please try again.\n");
            }
        }
    }

    private void printMenu() {
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

    }

    private void login() {
        while (true) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                continue;
            }

            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty.");
                continue;
            }

            boolean valid = accountRepo.findByUsernameAndPassword(username, password).isPresent();
            if (valid) {
                return;

            } else {
                System.out.println("Invalid username or password");
            }
        }
    }

    private void listMissions() {
        List<MoonMission> missions = missionRepo.findAll();
        if (missions.isEmpty()) {
            System.out.println("No missions found.");
        }
        for (MoonMission mission : missions) {
            System.out.println(mission.getSpacecraft());
        }
    }

    private void getMissionById() {
        System.out.println("Enter mission id: ");
        String input = scanner.nextLine().trim();
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

    private void countMissionsByYear() {
        System.out.println("Enter mission year: ");
        String input = scanner.nextLine().trim();
        int year;
        try {
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
        } else {
            System.out.println("Found " + numberOfMissions + " missions for year: " + year + "\n");
        }
    }

    private void createAccount() {
        System.out.println("Enter a first name: ");
        String firstName = scanner.nextLine().trim();
        System.out.println("Enter a last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.println("Enter ssn: ");
        String ssn = scanner.nextLine().trim();
        System.out.println("Choose a password: ");
        String password = scanner.nextLine().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            System.out.println("Invalid input. Names and password cannot be empty.\n");
            return;
        }

        if (!ssn.matches("\\d{10}") && !ssn.matches("\\d{6}-\\d{4}")) {
            System.out.println("ssn must be 10 digits or in format YYMMDD-XXXX.\n");
            return;
        }

        if (accountRepo.existsBySsn(ssn)) {
            System.out.println("An account with this ssn already exists.\n");
            return;
        }

        String baseName = (firstName.length() >= 3 ? firstName.substring(0, 3) : firstName) +
                (lastName.length() >= 3 ? lastName.substring(0, 3) : lastName);
        String name = baseName;
        int suffix = 1;

        while (accountRepo.existsByName(name)) {
            name = baseName + suffix;
            suffix++;
        }
        Account account = new Account(name, password, firstName, lastName, ssn);
        accountRepo.create(account);
        System.out.println("Account created with username: " + name + "\n");
    }


    private void updatePassword() {
        System.out.println("Enter user id: ");
        String input = scanner.nextLine().trim();
        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. user id must be a number.\n");
            return;
        }

        if (!accountRepo.existsByUserId(userId)) {
            System.out.println("No user with id: " + userId + "\n");
            return;
        }

        System.out.println("Enter a new password: ");
        String newPassword = scanner.nextLine().trim();
        if (newPassword.isEmpty()) {
            System.out.println("Password cannot be empty.\n");
            return;
        }

        boolean updated = accountRepo.updatePassword(userId, newPassword);
        if (updated) {
            System.out.println("Updated password for user with id: " + userId + "\n");
        } else {
            System.out.println("Failed to update password for user with id: " + userId + "\n");
        }

    }

    private void deleteAccount() {
        System.out.println("Enter user id to delete: ");
        String input = scanner.nextLine().trim();
        int userId;
        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. user id must be a number.\n");
            return;
        }
        if (!accountRepo.existsByUserId(userId)) {
            System.out.println("No user found with id: " + userId + "\n");
            return;
        }
        boolean deleted = accountRepo.deleteById(userId);
        if (deleted) {
            System.out.println("Account with id: " + userId + " has been deleted\n");
        } else {
            System.out.println("Failed to delete account with id: " + userId + "\n");
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

