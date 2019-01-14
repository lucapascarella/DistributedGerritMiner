package org.lucapascarella.beans;

import java.sql.Timestamp;

import org.lucapascarella.db.MySQL;

public class MyRevision extends MyBean {

    private MySQL mysql;

    public Long reviewsID;
    public String commitHash;
    public Integer revisionIndex;
    public Integer revisionSize;
    public Timestamp created;
    public String commitMessage;
    public String commitSubject;
    public String parentHash;

    public MyRevision(MySQL mysql) {
        this.mysql = mysql;
    }

    public MyRevision(MySQL mysql, Long reviewsID, String commitHash, Integer revisionIndex, Integer revisionSize, Timestamp created, String commitMessage, String commitSubject, String parentHash) {
        super();
        this.mysql = mysql;
        this.reviewsID = reviewsID;
        this.commitHash = commitHash;
        this.revisionIndex = revisionIndex;
        this.revisionSize = revisionSize;
        this.created = created;
        this.commitMessage = commitMessage;
        this.commitSubject = commitSubject;
        this.parentHash = parentHash;
    }

    public boolean checkTable() {
        String table = "revisions";
        String[] params = { "ID", "reviewsID", "commitHash", "revisionIndex", "revisionSize", "created", "commitMessage", "commitSubject", "parentHash" };
        String[] types = { "int(11)", "int(11)", "varchar(64)", "int(11)", "int(11)", "varchar(64)", "varchar(4096)", "varchar(4096)", "varchar(4096)" };
        String[] notNull = { "ID", "reviewsId", "revisionId" };
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
        String[] params = { "reviewsID", "commitHash", "revisionIndex", "revisionSize", "created", "commitMessage", "commitSubject", "parentHash" };
        String[] values = { String.valueOf(reviewsID), commitHash, formatString(revisionIndex), formatString(revisionSize), formatString(created), formatString(commitMessage), formatString(commitSubject), formatString(parentHash) };
        String key = mysql.insertValuesReturnID("revisions", params, values);
        return Long.parseLong(key);
    }
}
