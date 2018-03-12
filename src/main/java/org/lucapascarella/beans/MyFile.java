package org.lucapascarella.beans;

import org.lucapascarella.db.MySQL;

public class MyFile extends MyBean {

    private MySQL mysql;

    public Long revisionsID;
    public String file;
    public Integer linesInserted;
    public Integer linesDeleted;

    public MyFile(MySQL mysql) {
        this.mysql = mysql;
    }

    public MyFile(MySQL mysql, Long revisionsID, String file, Integer linesInserted, Integer linesDeleted) {
        super();
        this.mysql = mysql;
        this.revisionsID = revisionsID;
        this.file = file;
        this.linesInserted = linesInserted;
        this.linesDeleted = linesDeleted;
    }

    public boolean checkTable() {
        String table = "files";
        String[] params = { "ID", "revisionsID", "file", "linesInserted", "linesDeleted" };
        String[] types = { "int(11)", "int(11)", "varchar(512)", "int(11)", "int(11)" };
        String[] notNull = { "ID", "revisionsId", "file" };
        String[] autoIncrement = { "ID" };
        String[] unique = { "ID" };
        String[] primaryKey = { "ID" };
        String[] foreignKey = { "revisionsID", "revisions(ID)" };
        boolean rtn = mysql.createTableIfNotExists(table, params, types, notNull, autoIncrement, unique, primaryKey, foreignKey);
        if (!rtn)
            System.err.println("Please check this error with: " + table);
        return rtn;
    }

    public long sotre(boolean print) {
        // "ID", "revisionsID", "file", "linesInserted", "linesDeleted"
        String[] params = { "revisionsID", "file", "linesInserted", "linesDeleted" };
        String[] values = { String.valueOf(revisionsID), file, formatString(linesInserted), formatString(linesDeleted) };
        String key = mysql.insertValuesReturnID("files", params, values);
        return Long.parseLong(key);
    }

}
