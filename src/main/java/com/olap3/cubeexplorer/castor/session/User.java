package com.olap3.cubeexplorer.castor.session;

/**
 * A user from a {@link CrSession}
 */
public class User {

    /**
     * User id
     */
    private String id;

    /**
     * User name
     */
    private String name;

    /**
     * User group
     */
    private String group;

    public User(String id, String name, String group) {
        this.id = id;
        this.name = name;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
