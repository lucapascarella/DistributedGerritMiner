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
            minedResults = work(gerritID);
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
                list.add(work(startGerritID));
            } catch (RestApiException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            startGerritID++;
        }
        return list;
    }

    private MinedResults work(long gerritId) throws RestApiException, SQLException {
        MinedResults minedResult = new MinedResults();
        minedResult.setGerritId(gerritId);

        // Skip current Gerrit ID if this entry already exists
        ResultSet resultSet = mysql.selectQuery("reviews", "ID", "gerritId", MySQL.SELECT_EQUAL, String.valueOf(gerritId));
        // ResultSetMetaData md = resultSet.getMetaData();

        if (resultSet != null && !resultSet.next()) {
            // Query Gerrit API for getting the current Gerrit ID
            List<ChangeInfo> changeList = getChangeList(gerritId);
            if (!changeList.isEmpty()) {
                for (ChangeInfo ci : changeList) {
                    // Add a new review entry into DB
                    long reviewID = addReview(ci, gerritId);
                    minedResult.setReviewID(reviewID);

                    // Add a owner by email(s)
                    addOwner(ci, reviewID);

                    // Add reviewers by email(s)
                    addReviewers(ci, reviewID);

                    // Add messages
                    addMessages(ci, reviewID);

                    // Workaround for revisions
                    Map<String, RevisionInfo> revisions = ci.revisions;
                    RevisionInfo ri2 = revisions.get(ci.currentRevision);
                    int revisionsNumber = ri2._number;
                    /*
                     * I suspect a problem with 2.7 Gerrit API. In this case I can retrieve only last patch set. When we have only 1 patch set or the number of retrieved revisions is correct used the following method
                     * Otherwise find a better workaround
                     */
                    if (revisionsNumber == revisions.size()) {

                        // Change newChange = getCommentsPerReview(changes, gerritId, changeList, ci);
                        for (Map.Entry<String, RevisionInfo> revision : revisions.entrySet()) {
                            String revisionId = revision.getKey();
                            RevisionInfo ri = revision.getValue();
                            System.out.println("Gerrit ID: " + gerritId + ". Revision: " + ri._number);
                            // Add revision
                            MyRevision myRevision = new MyRevision(mysql, reviewID, revisionId, ri._number, ri.created, ri.commit.message, ri.commit.subject, ri.commit.parents.get(0).commit);
                            long revisionsID = myRevision.store(true);
                            // Add files
                            addFiles(ci, reviewID, revisionsID, revisionId, ri.files);
                        }
                    } else {
                        // TODO find an improvement for this problem
                        System.out.println("Workaround for Gerrit ID: " + gerritId + ". Revision: " + revisionsNumber);
                        for (int i = revisionsNumber; i > 0; i--) {
                            // Add revision
                            MyRevision myRevision = new MyRevision(mysql, reviewID, "", i, ri2.created, ri2.commit.message, ri2.commit.subject, ri2.commit.parents.get(0).commit);
                            long revisionsID = myRevision.store(true);
                            // Get Files and Comments
                            Map<String, FileInfo> filesMap = ri2.files; // getFilesPerRevision(changes, ci, String.valueOf(i));
                            Map<String, List<CommentInfo>> comments = getCommentPerRevision(changes, ci, String.valueOf(i));

                            if (filesMap != null) {
                                Set<String> files = filesMap.keySet();
                                for (String file : files) {
                                    // Add file
                                    FileInfo fi = filesMap.get(file);
                                    MyFile myFile = new MyFile(mysql, revisionsID, file, fi.linesInserted, fi.linesDeleted);
                                    long fileID = myFile.sotre(true);

                                    // Add comments
                                    addComments(comments, reviewID, file, fileID);
                                }
                            }
                        }
                    }

                }
            } else {
                System.out.println("Gerrit ID: " + gerritId + " empty!");
                minedResult.setReviewID(-1);
            }
        } else {
            long id = resultSet.getInt("ID");
            System.out.println("Gerrit ID: " + gerritId + " already present! ID: " + id);
            minedResult.setReviewID(id);
        }
        return minedResult;
    }

    /**
     * Add a review into 'reviews' table
     * 
     * @param ci
     *            a ChangeInfo object must be populated by API before call ths method
     * @param gerritId
     * @return the auto-increment ID used by DB
     */
    private long addReview(ChangeInfo ci, long gerritId) {
        // Add a new review entry
        String status = ci.status == null ? "" : ci.status.toString();
        MyReview myReview = new MyReview(mysql, gerritId, ci.changeId, ci.created, ci.submitted, ci.updated, ci.project, ci.branch, status);
        long reviewID = myReview.store(true);
        return reviewID;
    }

    /**
     * Add a owner into 'developers' table
     * 
     * @param ci
     *            a ChangeInfo object must be populated by API before call this method
     * @param reviewID
     *            the entry ID used by 'reviews' table
     */
    private void addOwner(ChangeInfo ci, long reviewID) {
        // Add owner by email(s)
        List<String> emails = ci.owner.secondaryEmails;
        if (emails != null) {
            for (String email : emails)
                new MyDeveloper(mysql, reviewID, "owner", ci.owner.name, ci.owner.username, ci.owner.email, email).store(true);
        } else {
            new MyDeveloper(mysql, reviewID, "owner", ci.owner.name, ci.owner.username, ci.owner.email, "").store(true);
        }
    }

    /**
     * Add all reviewers into 'developers' table
     * 
     * @param ci
     *            a ChangeInfo object must be populated by API before call this method
     * @param reviewID
     *            the entry ID used by 'reviews' table
     */
    private void addReviewers(ChangeInfo ci, long reviewID) throws RestApiException {
        List<ReviewerInfo> revs = getReviewers(ci);
        for (ReviewerInfo ri : revs) {
            List<String> emails = ri.secondaryEmails;
            if (emails != null) {
                for (String email : emails) {
                    new MyDeveloper(mysql, reviewID, "reviewer", ri.name, ri.username, ri.email, email).store(true);
                }
            } else {
                new MyDeveloper(mysql, reviewID, "reviewer", ri.name, ri.username, ri.email, "").store(true);
            }
        }
    }

    /**
     * Add all messages into 'messages' table
     * 
     * @param ci
     *            a ChangeInfo object must be populated by API before call this method
     * @param reviewID
     *            the entry ID used by 'reviews' table
     */
    private void addMessages(ChangeInfo ci, long reviewID) {
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
    }

    /**
     * Add all files of a patch set into 'files' table
     * 
     * @param ci
     *            a ChangeInfo object that must be populated by API before call this method
     * @param reviewID
     *            the entry ID used by 'reviews' table
     * @param revisionsID
     *            the entry ID used by 'revisions' table
     * @param revisionId
     *            the Gerrit revision ID (it is ah hash)
     * @param filesMap
     *            the files belonging to the current revision
     */
    private void addFiles(ChangeInfo ci, long reviewID, long revisionsID, String revisionId, Map<String, FileInfo> filesMap) {
        // Getting the list of files per revision
        Map<String, List<CommentInfo>> comments = getCommentPerRevision(changes, ci, revisionId);
        if (filesMap != null) {
            Set<String> files = filesMap.keySet();
            for (String file : files) {
                // Add file
                FileInfo fi = filesMap.get(file);
                MyFile myFile = new MyFile(mysql, revisionsID, file, fi.linesInserted, fi.linesDeleted);
                long fileID = myFile.sotre(true);
                // Add comments
                addComments(comments, reviewID, file, fileID);
            }
        }

    }

    /**
     * Add all comments of a patch set into 'files' table
     * 
     * @param comments
     *            a list of comments previously populated by calling the API
     * @param reviewID
     *            the entry ID used by 'reviews' table
     * @param file
     *            the current file
     * @param fileID
     *            the ntry ID used by 'files' table
     */
    private void addComments(Map<String, List<CommentInfo>> comments, long reviewID, String file, long fileID) {
        // Get comments per file
        List<CommentInfo> ciList = comments.get(file);
        if (ciList != null)
            for (CommentInfo commentInfo : ciList) {
                Long developerID = getDeveloperID(reviewID, commentInfo.author.name, commentInfo.author.email);
                new MyComment(mysql, fileID, developerID, commentInfo.line, commentInfo.message, commentInfo.updated).store(true);
            }
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

    private List<ReviewerInfo> getReviewers(ChangeInfo ci) throws RestApiException {
        List<ReviewerInfo> res = new ArrayList<ReviewerInfo>();
        int numTries = 1;
        while (true) {
            try {
                // Getting the list of xxx for each file
                res = changes.id(ci._number).listReviewers();
                break;
            } catch (com.urswolfer.gerrit.client.rest.http.HttpStatusException e) {
                System.out.println("Unable to get the list of the reviewers");
                break;
            } catch (com.google.gerrit.extensions.restapi.RestApiException e) {
                if (numTries > 100) {
                    System.out.println("Too many tries! Quitting...");
                    System.out.println("Sort key of the last element: " + ci._sortkey);
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

    private List<ChangeInfo> getChangeList(long id) {
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

}
