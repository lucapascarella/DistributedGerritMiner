package org.lucapascarella.beans;

import java.sql.Timestamp;

import org.lucapascarella.db.MySQL;

public class MyRevision extends MyBean {

    private MySQL mysql;

    public Long reviewsID;
    public String revisionId;
    public Integer index;
    public Timestamp created;
    public String commitMessage;
    public String commitSubject;
    public String commitHash;

    public MyRevision(MySQL mysql) {
        this.mysql = mysql;
    }

    public MyRevision(MySQL mysql, Long reviewsID, String revisionId, Integer index, Timestamp created, String commitMessage, String commitSubject, String commitHash) {
        super();
        this.mysql = mysql;
        this.reviewsID = reviewsID;
        this.revisionId = revisionId;
        this.index = index;
        this.created = created;
        this.commitMessage = commitMessage;
        this.commitSubject = commitSubject;
        this.commitHash = commitHash;
    }

    public boolean checkTable() {
        String table = "revisions";
        String[] params = { "ID", "reviewsID", "revisionId", "index", "created", "commitMessage", "commitSubject", "commitHash"};
        String[] types = { "int(11)", "int(11)", "varchar(64)", "int(11)", "varchar(64)", "varchar(4096)", "varchar(4096)", "varchar(4096)" };
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
        // "ID", "reviewsID", "revisionId", "index", "created", "commitMessage", "commitSubject", "commitHash"
        String[] params = { "reviewsID", "revisionId", "index", "created", "commitMessage", "commitSubject", "commitHash"};
        String[] values = { String.valueOf(reviewsID), revisionId, formatString(index), formatString(created), formatString(commitMessage), formatString(commitSubject), formatString(commitHash)};
        String key = mysql.insertValuesReturnID("revisions", params, values);
        return Long.parseLong(key);
    }
}
