package no.fint.provider.events.response;

import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.events.FintEvents;
import no.fint.provider.events.eventstate.EventState;
import no.fint.provider.events.eventstate.EventStateService;
import no.fint.provider.events.exceptions.UnknownEventException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ResponseService {

    @Autowired
    private EventStateService eventStateService;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private FintEvents fintEvents;

    public void handleAdapterResponse(Event event) {
        log.info("Event received: {}", event);
        if (event.isHealthCheck()) {
            event.setStatus(Status.UPSTREAM_QUEUE);
            fintEvents.sendUpstream(event);
        } else {
            Optional<EventState> state = eventStateService.get(event);
            if (state.isPresent()) {
                fintAuditService.audit(event, Status.ADAPTER_RESPONSE);
                event.setStatus(Status.UPSTREAM_QUEUE);
                fintEvents.sendUpstream(event);
                fintAuditService.audit(event, Status.UPSTREAM_QUEUE);
                eventStateService.remove(event);
            } else {
                log.error("EventState with corrId {} was not found. Either the Event has expired or the provider does not recognize the corrId. {}", event.getCorrId(), event);
                throw new UnknownEventException();
            }
        }
    }
}
