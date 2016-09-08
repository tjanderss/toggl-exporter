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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    @Autowired
    TimeEntryRetriever<TogglTimeEntry> retriever;

    @Autowired
    TimeEntryExporter exporter;

    private Calendar createCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar;
    }

    @Override
    public void run(String... strings) throws Exception {
        Calendar startCalendar = createCalendar();
        Calendar endCalendar = createCalendar();


        if (strings.length > 0) {
            log.info("Parsing command line arguments...");

            Map<String, String> args = new HashMap<>();

            for (int i=0; i < strings.length; i++) {
                String[] parts = strings[i].split("=");
                if ((parts.length != 2) || !parts[0].startsWith("--")) {
                    throw new IllegalArgumentException("Invalid parameter: " +strings[i]+ ", expected '--name=value'");
                }
                // convert to lowercase and remove prefix '--'
                String paramName = parts[0].toLowerCase().substring(2);
                args.put(paramName, parts[1]);
                log.info("> " +paramName+ ": " +parts[1]);
            }

            String strStart = args.get("startdate");
            String strEnd = args.get("enddate");

            if (strStart != null || strEnd != null) {
                if (strStart == null || strEnd == null) {
                    throw new IllegalArgumentException("Missing startDate or endDate from command line args! If one is provided, the other is required as well.");
                }
            }

            try {
                Date start = simpleDateFormat.parse(strStart);
                Date end = simpleDateFormat.parse(strEnd);
                startCalendar.setTime(start);
                endCalendar.setTime(end);

                if (end.before(start)) {
                    throw new IllegalArgumentException("End date can not be earlier than start date");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("Could not parse start- or enddate", e);
            }
        } else {
            log.info("No command line arguments specified. Using default values for start- and enddate (from monday of this week to end of current day).");
            startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }

        // End date is the end of the current day
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        Date endDate = endCalendar.getTime();

        // Start date is defined here as beginning of monday this week
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        Date startDate = startCalendar.getTime();

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
                log.warn("\n\n  !!! Warning !!!\n\n  Skipping entry: " +entry.getDescription()+ "\n  " +e.getMessage()+ "\n");
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
        SpringApplication.run(Main.class, args);
    }
}
