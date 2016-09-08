# toggl-exporter

(Work in progress..)

The exporter will by default retrieve time entries from the monday of current week (00:00) to the end of the current day. These defaults can be changed by providing the startDate and endDate command line args.
The Jira issue id's are expected in the beginning of a Toggl time entry description, e.g. _MYPROJ-1234 Did some funny stuff_. The rest of the description is added as a worklog comment in Jira.

1. Configure: Edit **src/main/resources/application.properties** to set your toggl & jira credentials and base uris

```
togglApiToken=xxxxxxx:api_token
togglUri=https://www.toggl.com/api/v8
jiraUri=https://myjira.com/rest/api/latest
jiraCredentials=username:password
```

2. Build: 

  `mvn clean install`

3. Run: Execute 

  `java -jar target/toggl-export-1.0-SNAPSHOT.jar [--startDate=dd-mm-yyyy --endDate=dd-mm-yyyy]`





