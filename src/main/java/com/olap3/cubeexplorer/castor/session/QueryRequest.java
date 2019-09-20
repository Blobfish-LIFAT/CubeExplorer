package com.olap3.cubeexplorer.castor.session;

import java.math.BigInteger;
import java.util.Date;

/**
 * A user query from a {@link Session}
 */
public class QueryRequest {

    /**
     * Query id
     */
    private int id;

    /**
     * Query string in MDX
     */
    private String query;

    /**
     * Execution date of the query
     */
    private Date datetime;

    /**
     * Execution duration in ms
     */
    private BigInteger duration;

    public QueryRequest(int id, String query, Date datetime, BigInteger duration) {
        this.id = id;
        this.query = query;
        this.datetime = datetime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public BigInteger getDuration() {
        return duration;
    }

    public void setDuration(BigInteger duration) {
        this.duration = duration;
    }

}
