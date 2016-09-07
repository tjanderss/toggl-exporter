package fi.toman.togglexport;

public class Worklog extends TimeEntry {

    private String issueId;

    private Worklog() {}

    public static Worklog fromTogglTimeEntry(final TimeEntry entry) throws TimeEntryException {
        String entryDescription = entry.getDescription();
        String[] parts = entryDescription.split(" ");
        String issueId = parts[0];

        if (!issueId.contains("-") || issueId.trim().contains(" ") ) {
            throw new TimeEntryException("Time entry description doesn't contain a Jira issue ID: " +entryDescription);
        }

        Worklog workLog = new Worklog();
        workLog.setDescription(entryDescription.replace(issueId, "").trim());
        workLog.setDate(entry.getDate());
        workLog.setDuration(entry.getDuration());
        workLog.setIssueId(issueId);
        return workLog;
    }

    public String getDurationAsString() {
        int hours = (int) Math.floor(getDuration() / 3600.0);
        int minutes = (int) Math.floor((getDuration() - (hours * 3600.0)) / 60.0);

        String duration = hours + "h " + minutes + "m";
        return duration;
    }

    @Override
    public String toString() {
        return getIssueId()+ "\n" +
                "\tcomment  : " +getDescription() + "\n"+
                "\tdate     : " +getDate()+ "\n" +
                "\tduration : " +getDurationAsString();
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }
}
