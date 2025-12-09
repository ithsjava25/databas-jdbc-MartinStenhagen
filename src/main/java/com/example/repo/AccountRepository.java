package com.example.repo;

import com.example.model.Account;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByUsernameAndPassword(String username, String password);
    Account create(Account account);
    boolean existsBySsn(String ssn);
    boolean existsByUserId(int userId);
    boolean updatePassword(int userId, String newPassword);
    boolean deleteById(int userId);
}
