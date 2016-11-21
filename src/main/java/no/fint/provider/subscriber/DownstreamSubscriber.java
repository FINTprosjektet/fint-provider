package no.fint.provider.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.fint.event.model.Event;
import no.fint.provider.sse.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class DownstreamSubscriber {

    @Autowired
    private SseService sseService;

    @Autowired
    private ObjectMapper objectMapper;

    public void receive(Map<String, String> headers, byte[] body) {
        try {
            Event event = objectMapper.readValue(body, Event.class);
            sseService.send(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
