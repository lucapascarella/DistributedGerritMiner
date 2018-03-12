package org.lucapascarella.beans;

import java.sql.Timestamp;

import org.lucapascarella.db.MySQL;

public class MyComment extends MyBean {

    private static final String table = "comments";

    private MySQL mysql;

    public Long filesID;
    public Long developerID;
    public Integer line;
    public String message;
    public Timestamp updated;

    public MyComment(MySQL mysql) {
        this.mysql = mysql;
    }

    public MyComment(MySQL mysql, Long filesID, Long developerID, Integer line, String message, Timestamp updated) {
        super();
        this.mysql = mysql;
        this.filesID = filesID;
        this.developerID = developerID;
        this.line = line;
        this.message = message;
        this.updated = updated;
    }

    public boolean checkTable() {
        String[] params = { "ID", "filesID", "developerID", "line", "message", "updated" };
        String[] types = { "int(11)", "int(11)", "int(11)", "int(11)", "varchar(4096)", "varchar(64)" };
        String[] notNull = { "ID", "filesID" };
        String[] autoIncrement = { "ID" };
        String[] unique = { "ID" };
        String[] primaryKey = { "ID" };
        String[] foreignKey = { "filesID", "files(ID)" };
        boolean rtn = mysql.createTableIfNotExists(table, params, types, notNull, autoIncrement, unique, primaryKey, foreignKey);
        if (!rtn)
            System.err.println("Please check this error with: " + table);
        return rtn;
    }

    public long store(boolean print) {
        // "ID", "filesID", "developerID", "line", "message", "updated"
        String[] params = { "filesID", "developerID", "line", "message", "updated" };
        String[] values = { String.valueOf(filesID), String.valueOf(developerID), formatString(line), formatString(message), formatString(updated) };
        String key = mysql.insertValuesReturnID(table, params, values);
        return Long.parseLong(key);
    }
}
