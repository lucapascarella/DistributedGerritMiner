package org.lucapascarella.beans;

import java.io.Serializable;

public class GerritBean implements Serializable {

    private static final long serialVersionUID = 1679403982960132297L;
    private String gerritUrl;
    private String gerritStart;
    private String gerritStop;
    private String finish;

    public GerritBean(String gerritUrl, String gerritStart, String gerritStop) {
        super();
        this.gerritUrl = gerritUrl;
        this.gerritStart = gerritStart;
        this.gerritStop = gerritStop;
    }

    public String getGerritUrl() {
        return gerritUrl;
    }

    public void setGerritUrl(String gerritUrl) {
        this.gerritUrl = gerritUrl;
    }

    public String getGerritStart() {
        return gerritStart;
    }

    public void setGerritStart(String gerritStart) {
        this.gerritStart = gerritStart;
    }

    public String getGerritStop() {
        return gerritStop;
    }

    public void setGerritStop(String gerritStop) {
        this.gerritStop = gerritStop;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

    public String getFinish() {
        return finish;
    }

}
