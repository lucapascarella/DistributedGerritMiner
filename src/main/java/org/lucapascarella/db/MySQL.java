package org.lucapascarella.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MySQL {

    public static final String SELECT_ALL = "*";
    public static final String SELECT_EQUAL = "=";

    private Connection connection;
    private Statement stmt;

    public MySQL(String host, String port, String dbName, String username, String password) {
        try {
            String myURL = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?autoReconnect=true&useSSL=false";
            connection = DriverManager.getConnection(myURL, username, password);
            stmt = connection.createStatement();
            // System.out.println("Database connected!");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public int insertQueryReturnId(String query) {
        int id = -1;
        try {
            id = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public ResultSet selectQuery(String query) {
        try {
            ResultSet rs = stmt.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String updateQuery(String table, String what, String value1, String where, String value2) {
        // List<String> list = null;
        // Prepare the SELECT string to query
        String query = "UPDATE `" + table + "` SET `" + what + "`='" + value1 + "' WHERE `" + where + "`='" + value2 + "'";
        // System.out.println(query);
        try {
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            int key = rs.next() ? rs.getInt(1) : 0;

            return String.valueOf(key);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet selectQuery(String table, String what, String where, String action, String value) {
        // List<String> list = null;
        // Prepare the SELECT string to query
        String query = "SELECT " + what + " FROM " + table + " WHERE " + where + " " + action + " " + value;
        // System.out.println(query);
        try {
            ResultSet rs = stmt.executeQuery(query);
            // ResultSetMetaData md = rs.getMetaData();
            // int columns = md.getColumnCount();
            // list = new ArrayList<String>();
            // while (rs.next()) {
            // String str = "";
            // for (int i = 0; i < columns; i++)
            // str += rs.getString(i);
            // list.add(str);
            // }
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method performs a simple SELECT query
     * 
     * @param table
     *            is the MySQL table name
     * @param param
     *            contain the parameter name
     * @param value
     *            contain a static array list of parameters type
     * 
     * @return return a set of statements
     * 
     */
    public List<HashMap<String, Object>> getList(String table, String param, String value) {
        // Prepare the string to create table
        String query = "SELECT * FROM " + table + " WHERE " + param + " " + value;
        ArrayList<HashMap<String, Object>> list = null;
        try {
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData md = rs.getMetaData();
            list = new ArrayList<HashMap<String, Object>>(0);
            int columns = md.getColumnCount();
            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<String, Object>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * This method insert a list of value into given table
     * 
     * @param table
     *            is the MySQL table name
     * @param params
     *            contain a static array list of parameters name
     * @param values
     *            contain a static array list of parameters values
     * @return return primaryKey or null in case of errors
     *
     */
    public String insertValuesReturnID(String table, String[] params, String[] values) {
        int i;
        // Return false if the user does not give a complete list of parameters name, types, and constraints
        if (params.length != values.length)
            return null;
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
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            int key = rs.next() ? rs.getInt(1) : 0;

            return String.valueOf(key);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
     *
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
            // System.out.println(query);
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            // e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
