package org.lucapascarella.gerrit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.lucapascarella.beans.Change;
import org.lucapascarella.db.MySQL;
import org.lucapascarella.utils.Config;
import org.lucapascarella.utils.PropDef;

import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ReviewerInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;

public class GerritMiner {

    private MySQL mysql;

    public GerritMiner(Config config) {
        // Create MySQL connection
        String sqlHost = config.getProp(PropDef.defaultDBHost[0]);
        String sqlPort = config.getProp(PropDef.defaultDBPort[0]);
        String sqlDBName = config.getProp(PropDef.defaultDBName[0]);
        String sqlUser = config.getProp(PropDef.defaultDBUser[0]);
        String sqlPass = config.getProp(PropDef.defaultDBPassword[0]);
        mysql = new MySQL(sqlHost, sqlPort, sqlDBName, sqlUser, sqlPass);

        // Check for 'reviews' tables
        String table = "user";
        String[] params = { "id", "name", "surname" };
        String[] types = { "int(11)", "varchar(64)", "varchar(255)" };
        String[] notNull = { "id", "surname" };
        String[] autoIncrement = { "id" };
        String[] unique = { "id", "name" };
        String[] primaryKey = { "id" };
        String[] foreignKey = { "name", "test2(id)" };
        mysql.createTableIfNotExists(table, params, types, notNull, autoIncrement, unique, primaryKey, foreignKey);

    }

    public void start(String gerritUrl, int startpoint, int endpoint) throws RestApiException, SQLException {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritAuthData.Basic authData = new GerritAuthData.Basic(gerritUrl);
        GerritApi gerritApi = gerritRestApiFactory.create(authData);
        Changes changes = gerritApi.changes();

        while (startpoint >= endpoint) {
            mine(changes, startpoint);
            startpoint--;
        }
    }

    private void mine(Changes changes, int id) throws RestApiException, SQLException {
        if (isAlreadyPresent(id)) {
            System.out.println(id + " is already present!");
            // return;
        }
        List<ChangeInfo> reviews = getChangesInRange(changes, id);

        if (reviews.isEmpty()) {
            return;
        }

        for (ChangeInfo c : reviews) {
            Change newChange = getCommentsPerReview(changes, id, reviews, c);
            List<ReviewerInfo> revs = getReviewers(changes, c);

            store(c, newChange, revs, id);
        }
    }

    private boolean isAlreadyPresent(int id) throws SQLException {
        PreparedStatement psReview = conn.prepareStatement("SELECT * FROM reviews where gerritid = ?");
        psReview.setInt(1, id);
        ResultSet rs = psReview.executeQuery();

        if (rs.next())
            return true;

        return false;
    }
}
