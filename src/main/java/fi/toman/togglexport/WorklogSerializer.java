package fi.toman.togglexport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class WorklogSerializer extends JsonSerializer<Worklog> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public void serialize(final Worklog jiraWorkLog, final JsonGenerator generator, final SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        generator.writeStartObject();
        generator.writeStringField("comment", jiraWorkLog.getDescription());
        String started = DATE_FORMAT.format(jiraWorkLog.getDate());
        generator.writeStringField("started", started);
        generator.writeNumberField("timeSpentSeconds", jiraWorkLog.getDuration());
        generator.writeEndObject();
    }
}