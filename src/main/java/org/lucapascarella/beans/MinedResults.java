package org.lucapascarella.beans;

import java.io.Serializable;

public class MinedResults implements Serializable {

    private static final long serialVersionUID = 1679403982960132297L;
    private long gerritId;
    private long reviewID;

    public long getGerritId() {
        return gerritId;
    }

    public void setGerritId(long gerritId) {
        this.gerritId = gerritId;
    }

    public long getReviewID() {
        return reviewID;
    }

    public void setReviewID(long reviewID) {
        this.reviewID = reviewID;
    }

}
