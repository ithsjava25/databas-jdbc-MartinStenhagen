package com.example.repo;

import com.example.model.Account;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByUsernameAndPassword(String username, String password);
    boolean existsBySsn(String ssn);
    Account create(Account account);
    boolean updatePassword(int userId, String newPassword);
    boolean deleteById(int userId);
}
