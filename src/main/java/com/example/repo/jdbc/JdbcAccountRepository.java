package com.example.repo.jdbc;

import com.example.DatabaseException;
import com.example.model.Account;
import com.example.repo.AccountRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class JdbcAccountRepository implements AccountRepository {

    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Account> findByUsernameAndPassword(String username, String password) {
        String query = "select * from account where name = ? and password = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.next()) return Optional.empty();

                Account account = new Account();
                account.id = result.getInt("user_id");
                account.name = result.getString("name");
                account.password = result.getString("password");
                return Optional.of(account);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute account query.", e);
        }
    }

    @Override
    public boolean existsBySsn(String ssn) {
        String query = "select 1 from account where ssn = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, ssn);
            try (ResultSet result = preparedStatement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute account query.", e);
        }
    }

    @Override
    public boolean existsByUserId(int userId) {
        String query = "select 1 from account where user_id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,userId);
            try(ResultSet result = preparedStatement.executeQuery()){
                return result.next();
            }

        }catch (SQLException e) {
            throw new DatabaseException("Failed to execute account query.", e);
        }

    }

    @Override
    public Account create(Account account) {
        String query = "insert into account(name, password, first_name, last_name, ssn) values (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, account.name);
            preparedStatement.setString(2, account.password);
            preparedStatement.setString(3, account.firstName);
            preparedStatement.setString(4, account.lastName);
            preparedStatement.setString(5, account.ssn);

            preparedStatement.executeUpdate();

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.id = keys.getInt(1);
                }
            }

            return account;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute account query.", e);
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPassword) {
        String query = "update account set password = ? where user_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, userId);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute account query.", e);
        }
    }

    @Override
    public boolean deleteById(int userId) {
        String query = "delete from account where user_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute account query.", e);
        }
    }
}

