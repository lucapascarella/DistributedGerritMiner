package org.lucapascarella.beans;

import java.io.Serializable;
import java.util.List;

public class MinedResults implements Serializable {

    private static final long serialVersionUID = 1679403982960132297L;
    private String mined;
    private MyReview review;
    private List<MyDeveloper> developers;
    private List<MyRevision> revisions;
    private List<MyFile> files;
    private List<MyComment> comments;

    public String getMined() {
        return mined;
    }

    public void setMined(String mined) {
        this.mined = mined;
    }

    public MyReview getReview() {
        return review;
    }

    public void setReview(MyReview review) {
        this.review = review;
    }

    public List<MyDeveloper> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<MyDeveloper> developers) {
        this.developers = developers;
    }

    public List<MyRevision> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<MyRevision> revisions) {
        this.revisions = revisions;
    }

    public List<MyFile> getFiles() {
        return files;
    }

    public void setFiles(List<MyFile> files) {
        this.files = files;
    }

    public List<MyComment> getComments() {
        return comments;
    }

    public void setComments(List<MyComment> comments) {
        this.comments = comments;
    }

}
