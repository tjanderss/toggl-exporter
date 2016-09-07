package fi.toman.togglexport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
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

@Component
public class TimeEntryExporter implements Exporter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${jiraUri}")
    private String jiraBaseUri;

    @Value("${jiraCredentials}")
    private String jiraCredentials;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void postWorklog(final Worklog workLog) throws TimeEntryException, IOException {
        URI uri;
        try {
            uri = new URIBuilder(jiraBaseUri + "/issue/" +workLog.getIssueId()+ "/worklog").build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid JIRA API URL", e);
        }

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(jiraCredentials);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, creds);
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setCredentialsProvider(provider);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(uri);
        post.addHeader(BasicScheme.authenticate(creds,"US-ASCII", false));
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        StringEntity input;
        try {
            input = new StringEntity(mapper.writeValueAsString(workLog));
        } catch (Exception e) {
            throw new TimeEntryException("Could ont serialize worklog", e);
        }
        input.setContentType("application/json");
        post.setEntity(input);

        log.info(post.toString());
/*
        System.out.println("Body:");
        try {
            input.writeTo(System.out);
        } catch (IOException e) {
            throw new IllegalStateException("Error while printing POST request body", e);
        }
        System.out.println("\n");
*/
        HttpResponse response = client.execute(post, localContext);
        ResponseHandler<String> handler = new BasicResponseHandler();
        log.info(handler.handleResponse(response));
    }



}
