package fi.toman.togglexport;

import java.util.List;
import java.util.Date;

public interface TimeEntryRetriever<T extends TimeEntry> {

    List<TogglTimeEntry> getTimeEntries(Date startDate, Date endDate) throws TimeEntryException;

}
