package org.lucapascarella.beans;

import java.io.Serializable;
import java.util.List;

public class MineRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private String gerritURL;
    private Long startGerritID;
    private Long stopGerritID;
    // private Boolean operation;
    private List<MinedResults> minedResults;
    private String mysqlHost, mysqlPort, mysqlName, mysqlUser, mysqlPassword;

    public MineRequest(String gerritURL, Long startGerritID, Long stopGerritID, String mysqlHost, String mysqlPort, String mysqlName, String mysqlUser, String mysqlPassword) {
        super();
        this.gerritURL = gerritURL;
        this.startGerritID = startGerritID;
        this.stopGerritID = stopGerritID;
        this.mysqlHost = mysqlHost;
        this.mysqlPort = mysqlPort;
        this.mysqlName = mysqlName;
        this.mysqlUser = mysqlUser;
        this.mysqlPassword = mysqlPassword;
    }

    public String getGerritURL() {
        return gerritURL;
    }

    public Long getStartGerritID() {
        return startGerritID;
    }

    public Long getStopGerritID() {
        return stopGerritID;
    }
    // public Boolean getOperation() {
    // return operation;
    // }

    // public void setOperation(Boolean operation) {
    // this.operation = operation;
    // }

    public List<MinedResults> getMinedResults() {
        return minedResults;
    }

    public void setMinedResults(List<MinedResults> remote) {
        this.minedResults = remote;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public void setMysqlHost(String mysqlHost) {
        this.mysqlHost = mysqlHost;
    }

    public String getMysqlPort() {
        return mysqlPort;
    }

    public void setMysqlPort(String mysqlPort) {
        this.mysqlPort = mysqlPort;
    }

    public String getMysqlName() {
        return mysqlName;
    }

    public void setMysqlName(String mysqlName) {
        this.mysqlName = mysqlName;
    }

    public String getMysqlUser() {
        return mysqlUser;
    }

    public void setMysqlUser(String mysqlUser) {
        this.mysqlUser = mysqlUser;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
