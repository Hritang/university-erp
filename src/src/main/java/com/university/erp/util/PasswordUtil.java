package com.university.erp.util;

import org.mindrot.jbcrypt.BCrypt;

//class for hashing and verifying passwords using jBCrypt

public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, storedHash);
    }

}