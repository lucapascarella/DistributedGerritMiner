package org.lucapascarella.beans;

import java.sql.Timestamp;

import org.lucapascarella.db.MySQL;

public class MyReview extends MyBean {

    private MySQL mysql;
    public Long ID;
    public Long gerritId;
    public String changeId;
    public Timestamp created;
    public Timestamp submitted;
    public Timestamp updated;
    public String project;
    public String branch;
    public String status;
    public String mineStatus;

    public MyReview(MySQL mysql) {
        this.mysql = mysql;
    }

    public MyReview(MySQL mysql, Long gerritId, String changeId, Timestamp created, Timestamp submitted, Timestamp updated, String project, String branch, String status, String mineStatus) {
        this.mysql = mysql;
        this.gerritId = gerritId;
        this.changeId = changeId;
        this.created = created;
        this.submitted = submitted;
        this.updated = updated;
        this.project = project;
        this.branch = branch;
        this.status = status;
        this.mineStatus = mineStatus;
    }

    public boolean checkTable() {
        String table = "reviews";
        String[] params = { "ID", "gerritId", "changeId", "created", "submitted", "updated", "project", "branch", "status", "mineStatus" };
        String[] types = { "int(11)", "varchar(64)", "varchar(64)", "varchar(64)", "varchar(64)", "varchar(64)", "varchar(256)", "varchar(256)", "varchar(64)", "varchar(256)" };
        String[] notNull = { "ID", "gerritId", "changeId" };
        String[] autoIncrement = { "ID" };
        String[] unique = { "ID", "gerritId", "changeId" };
        String[] primaryKey = { "ID" };
        String[] foreignKey = null;
        boolean rtn = mysql.createTableIfNotExists(table, params, types, notNull, autoIncrement, unique, primaryKey, foreignKey);
        if (!rtn)
            System.err.println("Please check this error with: " + table);
        return rtn;
    }

    public Long store(boolean print) {
        // "ID", "gerritId", "changeId", "created", "submitted", "updated", "project", "branch", "status"
        String[] params = { "gerritId", "changeId", "created", "submitted", "updated", "project", "branch", "status", "mineStatus" };
        String[] values = { String.valueOf(gerritId), changeId, formatString(created), formatString(submitted), formatString(updated), formatString(project), formatString(branch), formatString(status),
                formatString(mineStatus) };
        String key = mysql.insertValuesReturnID("reviews", params, values);
        return Long.parseLong(key);
    }

}
