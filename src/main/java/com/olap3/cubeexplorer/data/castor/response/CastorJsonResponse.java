package com.olap3.cubeexplorer.data.castor.response;

import java.util.List;

/**
 * This class is the json object used by the <i>CASTOR</i> client as an input to display all the intentional analytics
 * model's results of a user session.
 *
 */
public class CastorJsonResponse {

    /**
     * List of {@link CastorTable} to display
     */
    private List<CastorTable> castorTableList;

    public CastorJsonResponse(List<CastorTable> castorTableList) {
        this.castorTableList = castorTableList;
    }

    public List<CastorTable> getCastorTableList() {
        return castorTableList;
    }

    public void setCastorTableList(List<CastorTable> castorTableList) {
        this.castorTableList = castorTableList;
    }
}
