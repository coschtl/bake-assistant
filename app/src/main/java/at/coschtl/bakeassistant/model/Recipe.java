package at.coschtl.bakeassistant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Recipe implements Serializable {

    private final long  id;
    private String name;
    private final List<Step> steps;

    public Recipe(String name) {
        this(-1,name);
    }
    public Recipe(long id, String name) {
        this.id = id;
        this.name = name;
        this.steps = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public Step addNewStep() {
        Step step = new Step(id);
        steps.add(step);
        return step;
    }

    public Recipe addSteps(Collection<Step> steps) {
        this.steps.addAll(steps);
        return this;
    }

    public String toString() {
        return name;
    }

}
