package org.lucapascarella.beans;

import java.sql.Timestamp;

import org.lucapascarella.db.MySQL;

public class MyMessage extends MyBean {

    private static final String table = "messages";

    private MySQL mysql;

    public Long reviewsID;
    public Long developerID;
    public String message;
    public Timestamp date;

    public MyMessage(MySQL mysql) {
        this.mysql = mysql;
    }

    public MyMessage(MySQL mysql, Long reviewsID, Long developerID, String message, Timestamp date) {
        super();
        this.mysql = mysql;
        this.reviewsID = reviewsID;
        this.developerID = developerID;
        this.message = message;
        this.date = date;
    }

    public boolean checkTable() {
        String[] params = { "ID", "reviewsID", "developerID", "message", "date" };
        String[] types = { "int(11)", "int(11)", "int(11)", "varchar(10000)", "varchar(64)" };
        String[] notNull = { "ID", "reviewsID" };
        String[] autoIncrement = { "ID" };
        String[] unique = { "ID" };
        String[] primaryKey = { "ID" };
        String[] foreignKey = { "reviewsID", "reviews(ID)" };
        boolean rtn = mysql.createTableIfNotExists(table, params, types, notNull, autoIncrement, unique, primaryKey, foreignKey);
        if (!rtn)
            System.err.println("Please check this error with: " + table);
        return rtn;
    }

    public long store(boolean print) {
        // "ID", "reviewsID", "developerID", "message", "date"
        String[] params = { "reviewsID", "developerID", "message", "date" };
        String[] values = { String.valueOf(reviewsID), String.valueOf(developerID), formatString(message.substring(0, Math.min(message.length() - 1, 9999))), formatString(date) };
        String key = mysql.insertValuesReturnID(table, params, values);
        return Long.parseLong(key);
    }
}
