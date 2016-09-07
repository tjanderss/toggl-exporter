package fi.toman.togglexport;

import java.util.Set;

public class TogglTimeEntry extends TimeEntry {

    private long id;

    private Set<String> tags;

    @Override
    public String toString() {
        return "Time entry "+getId()+ "\n"+
                "\tdate       : " + getDate() + "\n" +
                "\tduration   : " + getDuration() + "s\n" +
                "\tdescription: " + getDescription();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(final Set<String> tags) {
        this.tags = tags;
    }
}
