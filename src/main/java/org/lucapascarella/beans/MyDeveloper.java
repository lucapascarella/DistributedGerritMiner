package org.lucapascarella.beans;

import org.lucapascarella.db.MySQL;

public class MyDeveloper extends MyBean {

    private MySQL mysql;

    public Long reviewsID;
    public String role;
    public String name;
    public String username;
    public String email;
    public String email2;

    public MyDeveloper(MySQL mysql) {
        this.mysql = mysql;
    }

    public MyDeveloper(MySQL mysql, Long reviewsID, String role, String name, String username, String email, String email2) {
        super();
        this.mysql = mysql;
        this.reviewsID = reviewsID;
        this.role = role;
        this.name = name;
        this.username = username;
        this.email = email;
        this.email2 = email2;
    }

    public boolean checkTable() {
        String table = "developers";
        String[] params = { "ID", "reviewsID", "role", "name", "username", "email", "email2" };
        String[] types = { "int(11)", "int(11)", "varchar(64)", "varchar(256)", "varchar(256)", "varchar(256)", "varchar(256)" };
        String[] notNull = { "ID", "reviewsId", "email" };
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
        // "ID", "reviewsID", "role", "name", "username", "email", "email2"
        String[] params = { "reviewsID", "role", "name", "username", "email", "email2" };
        String[] values = { String.valueOf(reviewsID), formatString(role), formatString(name), formatString(username), formatString(email), formatString(email2) };
        String key = mysql.insertValuesReturnID("developers", params, values);
        return Long.parseLong(key);
    }
}
