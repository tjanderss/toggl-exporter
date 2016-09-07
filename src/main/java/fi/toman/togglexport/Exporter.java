package fi.toman.togglexport;

import java.io.IOException;

public interface Exporter {

    void postWorklog(Worklog workLog) throws TimeEntryException, IOException;

}
