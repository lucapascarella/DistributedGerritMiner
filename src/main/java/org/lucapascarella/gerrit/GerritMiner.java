package org.lucapascarella.gerrit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lucapascarella.beans.MinedResults;
import org.lucapascarella.beans.MyComment;
import org.lucapascarella.beans.MyDeveloper;
import org.lucapascarella.beans.MyFile;
import org.lucapascarella.beans.MyMessage;
import org.lucapascarella.beans.MyReview;
import org.lucapascarella.beans.MyRevision;
import org.lucapascarella.db.MySQL;

import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.api.changes.ReviewerInfo;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ChangeMessageInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.FileInfo;
import com.google.gerrit.extensions.common.RevisionInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;

public class GerritMiner {
    private MySQL mysql;
    private String gettirUrl;
    private Changes changes;

    public GerritMiner() {
        super();
    }

    public GerritMiner(MySQL mysql, String gerritUrl) {
        super();
        this.mysql = mysql;
        this.gettirUrl = gerritUrl;
    }

    public void start() {
        // // Check DB tables
        // new MyReview(mysql).checkTable();
        // new MyDeveloper(mysql).checkTable();
        // new MyRevision(mysql).checkTable();
        // new MyFile(mysql).checkTable();
        // new MyComment(mysql).checkTable();
        // Create Gerrit API connection
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritAuthData.Basic authData = new GerritAuthData.Basic(gettirUrl);
        GerritApi gerritApi = gerritRestApiFactory.create(authData);
        changes = gerritApi.changes();
    }

    public MinedResults mine(int gerritID) {
        MinedResults minedResults = null;
        try {
            minedResults = work(changes, gerritID);
        } catch (RestApiException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return minedResults;
    }

    public List<MinedResults> mine(long startGerritID, long stopGerritID) {
        List<MinedResults> list = new ArrayList<MinedResults>();
        while (startGerritID <= stopGerritID) {
            try {
                list.add(work(changes, startGerritID));
            } catch (RestApiException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            startGerritID++;
        }
        return list;
    }

    private MinedResults work(Changes changes, long gerritId) throws RestApiException, SQLException {
        MinedResults minedResult = new MinedResults();

        // Skip current Gerrit ID if this entry already exists
        ResultSet resultSet = mysql.selectQuery("reviews", "ID", "gerritId", MySQL.SELECT_EQUAL, String.valueOf(gerritId));
        // ResultSetMetaData md = resultSet.getMetaData();

        if (resultSet != null && !resultSet.next()) {
            // Query Gerrit API for current the Gerrit ID
            List<ChangeInfo> changeList = getChangeList(changes, gerritId);
            if (!changeList.isEmpty()) {
                for (ChangeInfo ci : changeList) {
                    // Add a new review entry
                    String status = ci.status == null ? "" : ci.status.toString();
                    MyReview myReview = new MyReview(mysql, gerritId, ci.changeId, ci.created, ci.submitted, ci.updated, ci.project, ci.branch, status);
                    long reviewID = myReview.store(true);
                    // System.out.println("Gerrit ID: " + gerritId + ". ChangeID: " + ci.changeId + ". Created: " + ci.created + " by " + getAuthor(ci));
                    // int reviewID = insertReviewLuca(gerritId, ci.changeId, ci.created, ci.submitted, ci.updated, ci.project, ci.branch, ci.status.toString());

                    // Add owner by email(s)
                    List<String> emails = ci.owner.secondaryEmails;
                    if (emails != null) {
                        for (String email : emails)
                            new MyDeveloper(mysql, reviewID, "owner", ci.owner.name, ci.owner.username, ci.owner.email, email).store(true);
                    } else {
                        new MyDeveloper(mysql, reviewID, "owner", ci.owner.name, ci.owner.username, ci.owner.email, "").store(true);
                    }
                    // Add reviewers
                    List<ReviewerInfo> revs = getReviewers(changes, ci);
                    for (ReviewerInfo ri : revs) {
                        emails = ri.secondaryEmails;
                        if (emails != null) {
                            for (String email : emails) {
                                new MyDeveloper(mysql, reviewID, "reviewer", ri.name, ri.username, ri.email, email).store(true);
                            }
                        } else {
                            new MyDeveloper(mysql, reviewID, "reviewer", ri.name, ri.username, ri.email, "").store(true);
                        }
                    }

                    // Add messages
                    Collection<ChangeMessageInfo> messages = ci.messages;
                    if (messages != null) {
                        for (ChangeMessageInfo message : messages) {
                            if (message.author != null) {
                                long developerID = getDeveloperID(reviewID, message.author.name, message.author.email);
                                MyMessage myMessage = new MyMessage(mysql, reviewID, developerID, message.message, message.date);
                                myMessage.store(true);
                            }
                        }
                    }

                    // Workaround for revisions
                    Map<String, RevisionInfo> revisions = ci.revisions;
                    RevisionInfo ri2 = revisions.get(ci.currentRevision);
                    int revisionsNumber = ri2._number;
                    if (revisionsNumber == revisions.size()) {
                        System.out.println("Same size");
                        // Change newChange = getCommentsPerReview(changes, gerritId, changeList, ci);
                        for (Map.Entry<String, RevisionInfo> revision : revisions.entrySet()) {
                            String revisionId = revision.getKey();
                            RevisionInfo ri = revision.getValue();
                            System.out.println("Revision: " + ri._number);
                            // Add revision
                            MyRevision myRevision = new MyRevision(mysql, reviewID, revisionId, ri._number, ri.commit.message, ri.commit.subject, ri.commit.parents.get(0).commit);
                            long revisionsID = myRevision.store(true);
                            // System.out.println("ChangeID: " + ci.changeId + ". RevisionID: " + revisionId + ". " + ri.commit.message);
                            // int revisionsID = insertRevisionLuca(reviewID, revisionId, ri.commit.message, ri.commit.subject, ri.commit.parents.get(0).commit);

                            // Getting the list of files per revision
                            Map<String, FileInfo> filesMap = ri.files;
                            Map<String, List<CommentInfo>> comments = getCommentPerRevision(changes, ci, revisionId);
                            if (filesMap != null) {
                                Set<String> files = filesMap.keySet();
                                for (String file : files) {
                                    // Add file
                                    FileInfo fi = filesMap.get(file);
                                    MyFile myFile = new MyFile(mysql, revisionsID, file, fi.linesInserted, fi.linesDeleted);
                                    long fileID = myFile.sotre(true);
                                    // System.out.println("ChangeID: " + ci.changeId + ", revisionID: " + revisionId + ", file: " + file + ", " + fi.linesInserted + ", " + fi.linesDeleted);
                                    // int fileID = insertFileLuca(revisionsID, file, fi.linesInserted, fi.linesDeleted);

                                    // Get comments per file
                                    List<CommentInfo> ciList = comments.get(file);
                                    if (ciList != null)
                                        for (CommentInfo commentInfo : ciList) {
                                            Long developerID = getDeveloperID(reviewID, commentInfo.author.name, commentInfo.author.email);
                                            new MyComment(mysql, fileID, developerID, commentInfo.line, commentInfo.message, commentInfo.updated).store(true);
                                            // System.out.println("File: " + file + ", Line: " + commentInfo.line + ", Comment: " + commentInfo.message);
                                            // transId = insertCommentLuca(fileID, commentInfo.line, commentInfo.message);
                                        }
                                }
                            }
                        }
                    } else {

                        for (int i = revisionsNumber; i > 0; i--) {
                            // Add revision
                            MyRevision myRevision = new MyRevision(mysql, reviewID, "", i, ri2.commit.message, ri2.commit.subject, ri2.commit.parents.get(0).commit);
                            long revisionsID = myRevision.store(true);
                            // Get Files and Comments
                            Map<String, List<CommentInfo>> comments = getCommentPerRevision(changes, ci, String.valueOf(i));
                            Map<String, FileInfo> filesMap = ri2.files; // getFilesPerRevision(changes, ci, String.valueOf(i));

                            if (filesMap != null) {
                                Set<String> files = filesMap.keySet();
                                for (String file : files) {
                                    // Add file
                                    FileInfo fi = filesMap.get(file);
                                    MyFile myFile = new MyFile(mysql, revisionsID, file, fi.linesInserted, fi.linesDeleted);
                                    long fileID = myFile.sotre(true);
                                    // Get comments per file
                                    List<CommentInfo> ciList = comments.get(file);
                                    if (ciList != null)
                                        for (CommentInfo commentInfo : ciList) {
                                            Long developerID = getDeveloperID(reviewID, commentInfo.author.name, commentInfo.author.email);
                                            new MyComment(mysql, fileID, developerID, commentInfo.line, commentInfo.message, commentInfo.updated).store(true);
                                            // System.out.println("File: " + file + ", Line: " + commentInfo.line + ", Comment: " + commentInfo.message);
                                            // transId = insertCommentLuca(fileID, commentInfo.line, commentInfo.message);
                                        }
                                }
                            }
                        }
                    }

                }
            } else {
                System.out.println("Gerrit ID: " + gerritId + " empty!");
            }
        } else {
            System.out.println("Gerrit ID: " + gerritId + " already present! ID: " + resultSet.getInt("ID"));
        }
        return minedResult;
    }

    private long getDeveloperID(long reviewID, String name, String email) {
        String query = "SELECT ID FROM `developers` WHERE (`reviewsID` = " + reviewID + ") AND (`name` LIKE '" + name + "' OR `email` LIKE '" + email + "')";
        ResultSet resultSet = mysql.selectQuery(query);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // private Change getCommentsPerReview(Changes changes, int id, List<ChangeInfo> reviews, ChangeInfo c) throws SQLException, RestApiException {
    // System.out.println("#" + id + " changeID: " + c.changeId + "; submitted at: " + c.submitted + " by " + getAuthor(c));
    //
    // // Getting the revisions
    // Map<String, RevisionInfo> m = c.revisions;
    //
    // HashMap<String, Integer> totComments = new HashMap<String, Integer>();
    // HashMap<String, String> bodyComments = new HashMap<String, String>();
    // // HashMap<String, Set<String>> filesMap2 = new HashMap<String, Set<String>>();
    //
    // for (Map.Entry<String, RevisionInfo> entry : m.entrySet()) {
    // // Getting the list of the files per revision
    // Map<String, FileInfo> filesMap = entry.getValue().files;
    //
    // if (filesMap == null) {
    // continue;
    // }
    // Set<String> files = filesMap.keySet();
    // // filesMap2.put(key, value)
    //
    // String revisionId = entry.getKey();
    // System.out.println("ChangeID: " + c.changeId + ", revisionID: " + revisionId + ", files: " + files.toString());
    //
    // Map<String, List<CommentInfo>> comments = getCommentPerRevision(changes, c, revisionId);
    //
    // // Updating the total number of comments
    // HashMap<String, Integer> currentNumComments = getNumberOfComments(files, comments);
    // totComments = updateComments(totComments, currentNumComments);
    //
    // // Updating the total body of the comments
    // HashMap<String, String> currentBodyComments = getCommentsBody(files, comments);
    // bodyComments = updateBodyComments(bodyComments, currentBodyComments);
    // }
    // return new Change(totComments, bodyComments);
    // }

    private List<ReviewerInfo> getReviewers(Changes changes, ChangeInfo c) throws RestApiException {
        List<ReviewerInfo> res = new ArrayList<ReviewerInfo>();
        int numTries = 1;
        while (true) {
            try {
                // Getting the list of xxx for each file
                res = changes.id(c._number).listReviewers();
                break;
            } catch (com.urswolfer.gerrit.client.rest.http.HttpStatusException e) {
                System.out.println("Unable to get the list of the reviewers");
                break;
            } catch (com.google.gerrit.extensions.restapi.RestApiException e) {
                if (numTries > 100) {
                    System.out.println("Too many tries! Quitting...");
                    System.out.println("Sort key of the last element: " + c._sortkey);
                    System.exit(-1);
                }
                numTries++;
                System.out.println("--------REQUEST FAILED: doing a new request. Request number " + numTries);
            }
        }
        return res;
    }

    private Map<String, List<CommentInfo>> getCommentPerRevision(Changes changes, ChangeInfo c, String revisionId) {
        Map<String, List<CommentInfo>> comments = new HashMap<String, List<CommentInfo>>();
        int numTries = 1;
        while (true) {
            try {
                // Getting the list of comments for each file
                // comments = getComments(changes, c, revisionId);
                comments = changes.id(c._number).revision(revisionId).comments();
                break;
            } catch (com.urswolfer.gerrit.client.rest.http.HttpStatusException e) {
                System.out.println("Unable to get detail of change " + c.changeId);
                break;
            } catch (com.google.gerrit.extensions.restapi.RestApiException e) {
                if (numTries > 100) {
                    System.out.println("Too many tries! Quitting...");
                    System.out.println("Sort key of the last element: " + c._sortkey);
                    System.exit(-1);
                }
                numTries++;
                System.out.println("--------REQUEST FAILED: doing a new request. Request number " + numTries);
            }
        }
        return comments;
    }

    private List<ChangeInfo> getChangeList(Changes changes, long id) {
        List<ChangeInfo> reviews = null;
        int numTries = 1;

        while (true) {
            try {
                reviews = changes.query(Long.toString(id))
                        .withOptions(ListChangesOption.ALL_FILES, ListChangesOption.ALL_REVISIONS, ListChangesOption.DETAILED_ACCOUNTS, ListChangesOption.ALL_COMMITS, ListChangesOption.MESSAGES).get();
                break;
            } catch (com.google.gerrit.extensions.restapi.RestApiException e) {
                if (numTries > 100) {
                    System.out.println("Too many tries! Quitting...");
                    System.exit(-1);
                }
                numTries++;
                System.out.println("--------REQUEST FAILED: doing a new request. Request number " + numTries);
            }
        }
        return reviews;

        // FOR TESTING
        // try {
        // reviews = changes.query(Integer.toString(id)).withOptions(ListChangesOption.ALL_FILES, ListChangesOption.ALL_REVISIONS, ListChangesOption.DETAILED_ACCOUNTS).get();
        //// reviews = changes.query("I47de85fc2985dc6c46e31f571525b37f6ed8b23f").withOptions(ListChangesOption.ALL_FILES, ListChangesOption.ALL_REVISIONS, ListChangesOption.DETAILED_ACCOUNTS).get();
        // } catch (RestApiException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    // private Map<String, List<CommentInfo>> getComments(Changes changes, ChangeInfo c, String revisionId) throws RestApiException {
    // return changes.id(c._number).revision(revisionId).comments();
    // }

    // private String getAuthor(ChangeInfo c) {
    // if (c.owner.name != null) {
    // return formatString(c.owner.name);
    // }
    // return null;
    // }

    public HashMap<String, String> getCommentsBody(Set<String> files, Map<String, List<CommentInfo>> comments) {
        HashMap<String, String> commentsBody = new HashMap<String, String>();
        for (String file : files) {
            List<CommentInfo> commentsList = comments.getOrDefault(file, null);

            if (commentsList != null) {
                commentsBody.put(file, String.join("\n###NEWCOMMENT###\n", commentsList.stream().map(x -> x.message).toArray(size -> new String[size])));
            }
        }
        return commentsBody;
    }

    // private HashMap<String, Integer> updateComments(HashMap<String, Integer> totComments, HashMap<String, Integer> currentComments) {
    // for (String file : currentComments.keySet()) {
    // totComments.put(file, totComments.getOrDefault(file, 0) + currentComments.getOrDefault(file, 0));
    // }
    // return totComments;
    // }
    //
    // private HashMap<String, String> updateBodyComments(HashMap<String, String> bodyComments, HashMap<String, String> currentBodyComments) {
    // for (String file : currentBodyComments.keySet()) {
    // bodyComments.put(file, bodyComments.getOrDefault(file, "") + "\n###NEWREVISION###\n" + currentBodyComments.getOrDefault(file, ""));
    // }
    // return bodyComments;
    // }

    // public Integer insertReview(Review r) throws SQLException {
    // String query = "INSERT INTO " + databaseReviews + " (gerritid, changeid, author, created, submitted, updated, file, project, numfiles, numreviewers, numcomments) VALUES('" + r.getGerritId() + "',
    // '" + r.changeId
    // + "', '" + r.author + "', '" + r.createdAt + "', '" + r.submittedAt + "', '" + r.updatedAt + "', '" + r.filename + "', '" + r.project + "', '" + r.numFiles + "', '" + r.numReviewers + "', "
    // + r.numComments + ")";
    // stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
    // ResultSet generatedKeys = stmt.getGeneratedKeys();
    // if (generatedKeys.next()) {
    // return generatedKeys.getInt(1);
    // } else {
    // throw new SQLException("Creating review failed: no ID obtained.");
    // }
    // }

    // public void insertComments(String changeId, Integer id, String bodyComments) throws SQLException {
    // String formattedComment = formatString(bodyComments);
    // String query = "INSERT INTO " + databaseComments + " (id, changeid, body) VALUES('" + id + "', '" + changeId + "', '" + formattedComment + "')";
    // stmt.executeUpdate(query);
    // }
    //
    // public void insertReviewer(String changeId, String reviewer, String email) throws SQLException {
    // String query = "INSERT INTO " + databaseReviewers + " (changeid, reviewer, email) VALUES('" + changeId + "', '" + reviewer + "', '" + email + "')";
    // stmt.executeUpdate(query);
    //
    // }
    //
    // public void insertStatus(String gerritId, String status) throws SQLException {
    // String query = "INSERT INTO " + databaseStatus + " (id, status) VALUES('" + gerritId + "', '" + status + "')";
    // stmt.executeUpdate(query);
    //
    // }

    public HashMap<String, Integer> getNumberOfComments(Set<String> files, Map<String, List<CommentInfo>> comments) {
        HashMap<String, Integer> totComments = new HashMap<String, Integer>();
        for (String file : files) {
            List<CommentInfo> comment = comments.getOrDefault(file, null);
            if (comment != null) {
                totComments.put(file, comment.size());
            } else {
                totComments.put(file, 0);
            }
        }
        return totComments;
    }

    public String formatString(String toFormat) {
        if (toFormat != null)
            return toFormat.replaceAll("\\'", "").replaceAll("\"", "").replaceAll("%", "").replace("\\", "");
        return "";
    }

    public String formatString(Timestamp toFormat) {
        if (toFormat != null)
            return toFormat.toString().replaceAll("\\'", "").replaceAll("\"", "").replaceAll("%", "").replace("\\", "");
        return "";
    }

    public String formatString(Integer toFormat) {
        if (toFormat != null)
            return toFormat.toString().replaceAll("\\'", "").replaceAll("\"", "").replaceAll("%", "").replace("\\", "");
        return "0";
    }

    // private void store(ChangeInfo c, Change newChange, List<ReviewerInfo> revs, int id) throws SQLException {
    // HashMap<String, Integer> fileIds = new HashMap<String, Integer>();
    //
    // if (isAlreadyPresent(newChange, c)) {
    // System.out.println(c.changeId + " already present! ");
    // return;
    // }
    // for (String file : newChange.getTotComments().keySet()) {
    // String submitted = (c.submitted != null) ? c.submitted.toString() : "1000-01-01 00:00:0.0";
    // Review r = new Review(Integer.toString(id), c.changeId, getAuthor(c), c.created.toString(), submitted, c.updated.toString(), formatString(file), formatString(c.project),
    // newChange.getTotComments().get(file),
    // revs.size(), newChange.getTotComments().size());
    // fileIds.put(file, insertReview(r));
    //
    // insertComments(c.changeId, fileIds.get(file), newChange.getBodyComments().get(file));
    // }
    //
    // for (ReviewerInfo ri : revs) {
    // insertReviewer(c.changeId, formatString(ri.name), formatString(ri.email));
    // }
    // insertStatus(Integer.toString(id), c.status == null ? "UNKNOWN" : c.status.toString());
    // }

    // private boolean isAlreadyPresent(Change newChange, ChangeInfo c) throws SQLException {
    // for (String file : newChange.getTotComments().keySet()) {
    // PreparedStatement psReview = conn.prepareStatement("SELECT * FROM reviews where changeid = ? and file = ?");
    // psReview.setString(1, c.changeId);
    // psReview.setString(2, file);
    // ResultSet rs = psReview.executeQuery();
    //
    // if (rs.next())
    // return true;
    // }
    // return false;
    //
    // }

    // private boolean isAlreadyPresent(int id) throws SQLException {
    // PreparedStatement psReview = conn.prepareStatement("SELECT * FROM reviews where gerritid = ?");
    // psReview.setInt(1, id);
    // ResultSet rs = psReview.executeQuery();
    //
    // if (rs.next())
    // return true;
    //
    // return false;
    // }

    // NEW CHANGES

    // private int insertReviewLuca(Integer gerritId, String changeId, Timestamp created, Timestamp submitted, Timestamp updated, String project, String branch, String status) {
    // // "ID", "gerritId", "changeId", "created", "submitted", "updated", "project", "branch", "status"
    // String[] params = { "gerritId", "changeId", "created", "submitted", "updated", "project", "branch", "status" };
    // String[] values = { String.valueOf(gerritId), changeId, formatString(created), formatString(submitted), formatString(updated), formatString(project), formatString(branch), formatString(status) };
    // String key = mysql.insertValuesReturnID("reviews", params, values);
    // return Integer.parseInt(key);
    // }

    // private int insertDeveloperLuca(Integer reviewsID, String role, String name, String username, String email, String email2) {
    // // "ID", "reviewsID", "role", "name", "username", "email", "email2"
    // String[] params = { "reviewsID", "role", "name", "username", "email", "email2" };
    // String[] values = { String.valueOf(reviewsID), formatString(role), formatString(name), formatString(username), formatString(email), formatString(email2) };
    // String key = mysql.insertValuesReturnID("developers", params, values);
    // return Integer.parseInt(key);
    // }

    // private int insertRevisionLuca(Integer reviewsID, String revisionId, String commitMessage, String commitSubject, String commitHash) {
    // // "ID", "reviewsID", "revisionId", "commitMessage", "commitSubject", "commitHash"
    // String[] params = { "reviewsID", "revisionId", "commitMessage", "commitSubject", "commitHash" };
    // String[] values = { String.valueOf(reviewsID), revisionId, formatString(commitMessage), formatString(commitSubject), formatString(commitHash) };
    // String key = mysql.insertValuesReturnID("revisions", params, values);
    // return Integer.parseInt(key);
    // }

    // private int insertFileLuca(Integer revisionsID, String file, Integer linesInserted, Integer linesDeleted) {
    // // "ID", "revisionsID", "file", "linesInserted", "linesDeleted"
    // String[] params = { "revisionsID", "file", "linesInserted", "linesDeleted" };
    // String[] values = { String.valueOf(revisionsID), file, formatString(linesInserted), formatString(linesDeleted) };
    // String key = mysql.insertValuesReturnID("files", params, values);
    // return Integer.parseInt(key);
    // }

    // private int insertCommentLuca(Integer filesID, Integer line, String message) {
    // // "ID", "revisionsID", "file", "line", "message"
    // String[] params = { "filesID", "line", "message" };
    // String[] values = { String.valueOf(filesID), formatString(line), formatString(message) };
    // String key = mysql.insertValuesReturnID("comments", params, values);
    // return Integer.parseInt(key);
    // }

}
