package com.exam.system.service;

import com.exam.system.exception.InvalidCredentialsException;
import com.exam.system.exception.UserNotFoundException;
import com.exam.system.model.Student;
import com.exam.system.model.User;
import com.exam.system.repository.FileHandler;

import java.util.Map;

/**
 * Service managing user authentication, registration, and state persistence.
 */
public class AuthService {
    private Map<String, User> userDatabase;
    private FileHandler fileHandler;

    public AuthService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.userDatabase = fileHandler.loadUsers();
    }

    /**
     * Authenticates a user with username and password.
     * Throws UserNotFoundException or InvalidCredentialsException.
     */
    public User login(String username, String password) throws UserNotFoundException, InvalidCredentialsException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidCredentialsException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidCredentialsException("Password cannot be empty.");
        }

        String trimmedUser = username.trim();
        User user = userDatabase.get(trimmedUser);

        if (user == null) {
            throw new UserNotFoundException("No account found with username: '" + trimmedUser + "'");
        }

        if (!user.validatePassword(password)) {
            throw new InvalidCredentialsException("Incorrect password. Please verify your credentials.");
        }

        if (user.hasLegacyPassword()) {
            user.upgradeLegacyPassword(password);
            fileHandler.saveUsers(userDatabase);
        }

        return user;
    }

    /**
     * Registers a new student and persists to disk.
     */
    public boolean registerStudent(Student student) {
        if (student == null || userDatabase.containsKey(student.getUsername())) {
            return false;
        }
        userDatabase.put(student.getUsername(), student);
        fileHandler.saveUsers(userDatabase);
        return true;
    }

    public Map<String, User> getAllUsers() {
        return userDatabase;
    }

    public void reload() {
        this.userDatabase = fileHandler.loadUsers();
    }
}
