package fi.toman.togglexport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private ObjectMapper mapper;

    public AppConfig() {
        JsonFactory factory = new JsonFactory();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TogglTimeEntry.class, new TogglTimeEntryDeserializer());
        module.addSerializer(Worklog.class, new WorklogSerializer());
        mapper = new ObjectMapper(factory);
        mapper.registerModule(module);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return mapper;
    }
}