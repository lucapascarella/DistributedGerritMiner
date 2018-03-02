package org.lucapascarella.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {

    private Connection connection;
    private Statement stmt;

    public MySQL(String host, String port, String dbName, String username, String password) {
        try {
            String myURL = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
            connection = DriverManager.getConnection(myURL, username, password);
            stmt = connection.createStatement();
            System.out.println("Database connected!");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public boolean insertComments(String table, String[] params, String[] values) {
        int i;
        // Return false if the user does not give a complete list of parameters name, types, and constraints
        if (params.length != values.length)
            return false;
        // Prepare the string to insert values into the table
        String query = "INSERT INTO " + table + " (";
        for (i = 0; i < params.length; i++) {
            query += "`" + params[i] + "`";
            if (i != params.length - 1)
                query += ", ";
        }
        query += ") VALUES (";
        for (i = 0; i < values.length; i++) {
            query += "'" + values[i] + "'";
            if (i != values.length - 1)
                query += ", ";
        }
        query += ");";
        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This method add a table to MySQL DB if not exists
     * 
     * @param table
     *            is the MySQL table name
     * @param params
     *            contain a static array list of parameters name
     * @param types
     *            contain a static array list of parameters type
     * @param primaryKey
     *            if specified is the primary key
     * @return return true if success
     * @throws SQLException
     */
    public boolean createTableIfNotExists(String table, String[] params, String[] types, String[] notNull, String[] autoIncrement, String[] unique, String[] primaryKey, String[] foreignKey) {

        int i;
        // Return false if the user does not give a complete list of parameters name, types, and constraints
        if (params.length != types.length)
            return false;

        // Prepare the string to create table
        String query = "CREATE TABLE IF NOT EXISTS `" + table + "` (";
        for (i = 0; i < params.length; i++) {
            query += "`" + params[i] + "` " + types[i];
            // Handle not null
            if (notNull != null)
                for (String nn : notNull)
                    if (params[i].equals(nn))
                        query += " NOT NULL";
            // Handle auto increment
            if (autoIncrement != null)
                for (String ai : autoIncrement)
                    if (params[i].equals(ai))
                        query += " AUTO_INCREMENT";
            if (i != params.length - 1)
                query += ", ";
        }
        // Handle primary key
        if (primaryKey != null) {
            query += ", PRIMARY KEY (";
            for (i = 0; i < primaryKey.length; i++) {
                query += "`" + primaryKey[i] + "`";
                if (i != primaryKey.length - 1)
                    query += ", ";
            }
            query += ")";
        }
        // Handle Unique parameters
        if (unique != null) {
            query += ", UNIQUE (";
            for (i = 0; i < unique.length; i++) {
                query += "`" + unique[i] + "`";
                if (i != unique.length - 1)
                    query += ", ";
            }
            query += ")";
        }
        // Handle foreign key
        // 0-element is the local parameter-name and 1-element is the name_table(primary-key-sequence)
        //
        if (foreignKey != null && foreignKey.length == 2) {
            query += ", FOREIGN KEY (" + foreignKey[0] + ") REFERENCES " + foreignKey[1];
        }

        query += ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
        try {
            System.out.println(query);
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            // e.printStackTrace();
            return false;
        }
        return true;
    }
}
