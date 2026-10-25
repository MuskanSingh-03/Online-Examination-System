package com.exam.system.model;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Abstract base class modeling system users.
 * Implements Serializable to support file persistence across reboots.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Encapsulated private attributes
    private String username;
    private String password;
    private byte[] passwordSalt;
    private String fullName;
    private String email;
    private String role;

    public User(String username, String password, String fullName, String email, String role) {
        this.username = username;
        setPassword(password);
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean validatePassword(String inputPassword) {
        if (inputPassword == null || password == null) return false;
        if (passwordSalt == null) return password.equals(inputPassword); // upgrade old local accounts at next login
        return Arrays.equals(hash(inputPassword.toCharArray(), passwordSalt), decode(password));
    }

    public void setPassword(String password) {
        if (password == null) throw new IllegalArgumentException("Password cannot be null");
        this.passwordSalt = new byte[16];
        new SecureRandom().nextBytes(passwordSalt);
        this.password = encode(hash(password.toCharArray(), passwordSalt));
    }

    public boolean hasLegacyPassword() { return passwordSalt == null; }
    public void upgradeLegacyPassword(String inputPassword) { if (hasLegacyPassword() && validatePassword(inputPassword)) setPassword(inputPassword); }
    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 120_000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException ex) { throw new IllegalStateException("Password hashing is unavailable", ex); }
        finally { spec.clearPassword(); Arrays.fill(password, '\0'); }
    }
    private static String encode(byte[] bytes) { return java.util.Base64.getEncoder().encodeToString(bytes); }
    private static byte[] decode(String encoded) { try { return java.util.Base64.getDecoder().decode(encoded); } catch (IllegalArgumentException ex) { return new byte[0]; } }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Abstract method enforcing polymorphic dashboard display for subclasses
    public abstract void displayDashboard();

    public void displayProfile() {
        System.out.println("---------------------------------------------");
        System.out.println("Role:      " + role);
        System.out.println("Username:  " + username);
        System.out.println("Full Name: " + fullName);
        System.out.println("Email:     " + email);
        System.out.println("---------------------------------------------");
    }
}
