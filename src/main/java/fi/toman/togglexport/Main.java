package fi.toman.togglexport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TimeEntryRetriever<TogglTimeEntry> retriever;

    @Autowired
    TimeEntryExporter exporter;

    @Override
    public void run(String... strings) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        // End date is the end of the current day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        Date endDate = calendar.getTime();

        // Start date is defined here as beginning of monday this week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();

        List<TogglTimeEntry> entries;
        try {
            entries = retriever.getTimeEntries(startDate, endDate);
        } catch (TimeEntryException e) {
            throw new IllegalStateException(e);
        }

        int failureCount = 0;
        for (TogglTimeEntry entry : entries) {
            if (entry == null) {
                failureCount++;
                continue;
            }
            try {
                Worklog worklog = Worklog.fromTogglTimeEntry(entry);
                log.info("\nPosting worklog: "+ worklog);
                exporter.postWorklog(worklog);
            } catch (TimeEntryException e) {
                log.warn("\nWarning!! Skipping entry: " +entry.getDescription()+ "\n\n" +e.getMessage()+"\n");
                log.debug("", e);
                failureCount++;
            }
        }

        if (failureCount == 0) {
            log.info("Finished!\nAll " +entries.size()+ " worklogs were succesfully posted to Jira");
        } else {
            log.warn("Finished with warnings: " +failureCount + " of " + entries.size()+ " worklogs could not be posted to Jira");
        }
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class);
    }
}
