package fi.toman.togglexport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class TogglTimeEntryDeserializer extends JsonDeserializer<TogglTimeEntry> {

    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_DURATION = "duration";
    private static final String ATTR_STARTDATE = "start";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public TogglTimeEntry deserialize(final JsonParser jp, final DeserializationContext ctx) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String description = node.get(ATTR_DESCRIPTION).asText();
        int duration = node.get(ATTR_DURATION).intValue();
        String startDateAsString = node.get(ATTR_STARTDATE).asText();

        List<String> tags = node.findValuesAsText("tags");
        Date startDate;
        try {
            startDate = DateUtil.parseFromISO8601String(startDateAsString);
        } catch (ParseException e) {
            log.error("Could not parse time entry start date. Returning null.", e);
            log.error("Problematic time entry description: " +description);
            log.error("Problematic time entry start date: " +startDateAsString);
            log.error("Problematic time entry duration: " +duration);
            return null;
        }

        TogglTimeEntry entry = new TogglTimeEntry();
        entry.setDescription(description);
        entry.setDuration(duration);
        entry.setDate(startDate);
        return entry;
    }
}