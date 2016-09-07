package fi.toman.togglexport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

@Component
public class TogglTimeEntryRetriever implements TimeEntryRetriever<TogglTimeEntry> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${togglApiToken}")
    private String togglApiToken;

    @Value("${togglUri}")
    private String togglUri;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public List<TogglTimeEntry> getTimeEntries(final Date startDate, final Date endDate) throws TimeEntryException {
        URI uri;
        try {
            uri = new URIBuilder(togglUri + "/time_entries").
                    addParameter("start_date", DateUtil.formatToISO8601String(startDate)).
                    addParameter("end_date", DateUtil.formatToISO8601String(endDate)).
                    build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid Toggl API URL", e);
        }

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(togglApiToken);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, creds);
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setCredentialsProvider(provider);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
        get.addHeader(BasicScheme.authenticate(creds,"US-ASCII", false));
        get.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        log.info("Getting time entries from Toggl");
        log.info("\tStart date: " +startDate.toString());
        log.info("\tEnd date: " +endDate.toString());
        log.info("Request: " +get + "\n");

        HttpResponse response;
        try {
            response = client.execute(get, localContext);
        } catch (IOException e) {
            throw new TimeEntryException("Error while retrieving time entries from Toggl", e);
        }
        ResponseHandler<String> handler = new BasicResponseHandler();

        List<TogglTimeEntry> entries;
        try {
            entries = mapper.readValue(handler.handleResponse(response), new TypeReference<List<TogglTimeEntry>>(){});
        } catch (IOException e) {
            throw new TimeEntryException("Error while deserializing time entries from Toggl", e);
        }
        return entries;
    }
}
