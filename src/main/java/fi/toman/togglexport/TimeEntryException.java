package fi.toman.togglexport;

public class TimeEntryException extends Exception {

    public TimeEntryException(String msg) {
        super(msg);
    }

    public TimeEntryException(String msg, Throwable t) {
        super(msg, t);
    }
}
