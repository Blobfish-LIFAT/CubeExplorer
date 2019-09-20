package com.olap3.cubeexplorer.castor.session;


import java.util.List;

/**
 * Json model for user session (see "resources/logs")
 */
public class Session {

    /**
     * User of the session
     */
    private User user;

    /**
     * Cube name
     */
    private String cube;

    /**
     * Title or goal of the session
     */
    private String title;

    /**
     * Comment on the session
     */
    private String comment;

    /**
     * Query list performed during the session
     */
    private List<QueryRequest> queries;

    public Session(User user, String cube, String title, String comment, List<QueryRequest> queries) {
        this.user = user;
        this.cube = cube;
        this.title = title;
        this.comment = comment;
        this.queries = queries;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCube() {
        return cube;
    }

    public void setCube(String cube) {
        this.cube = cube;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<QueryRequest> getQueries() {
        return queries;
    }

    public void setQueries(List<QueryRequest> queries) {
        this.queries = queries;
    }
}
