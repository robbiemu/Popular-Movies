package xyz.selfenrichment.robertotomas.popularmovies.SQLite;
// Created by RobertoTomás on 0031, 31, 3, 2016.

/**
 * helper functions
 */
public class DBUtilities {
    static Boolean isValidType(String type) {
        return (type.equals("VARCHAR(255)")) || type.equals("VARCHAR(64)") || (type.equals("INTEGER")) || (type.equals("TEXT"));
    }
}
