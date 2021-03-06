package at.coschtl.bakeassistant.model;

import java.io.Serializable;

public class Action implements Serializable {

    private final long id;
    private final String name;

    public Action(String name) {
        this(-1, name);
    }

    public Action(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
