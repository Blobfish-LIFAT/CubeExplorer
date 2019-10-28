package com.olap3.cubeexplorer.data.castor.session;


import java.util.ArrayList;
import java.util.List;

/**
 * Json model for user session (see "resources/logs")
 */
public class CrSession {

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

    public CrSession(User user, String cube, String title, String comment, List<QueryRequest> queries) {
        this.user = user;
        this.cube = cube;
        this.title = title;
        this.comment = comment;
        this.queries = queries;
    }

    public CrSession(User user, String cube, String title, String comment) {
        this.user = user;
        this.cube = cube;
        this.title = title;
        this.comment = comment;
        queries = new ArrayList<>();
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

    @Override
    public String toString() {
        return "Session{" +
                "user=" + user +
                ", cube='" + cube + '\'' +
                ", title='" + title + '\'' +
                ", comment='" + comment + '\'' +
                ", queries=" + queries +
                '}';
    }
}
