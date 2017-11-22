package no.fint.provider.events.subscriber;

import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.events.FintEventListener;
import no.fint.provider.events.Constants;
import no.fint.provider.events.ProviderProps;
import no.fint.provider.events.eventstate.EventStateService;
import no.fint.provider.events.sse.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DownstreamSubscriber implements FintEventListener {

    @Autowired
    private SseService sseService;

    @Autowired
    private EventStateService eventStateService;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private ProviderProps providerProps;

    @Override
    public void accept(Event event) {
        log.info("Event received: {} for {}", event.getAction(), event.getOrgId());
        if (event.isHealthCheck()) {
            event.addObject(new Health(Constants.COMPONENT, HealthStatus.RECEIVED_IN_PROVIDER_FROM_CONSUMER));
        }

        sseService.send(event);
        fintAuditService.audit(event, Status.DELIVERED_TO_ADAPTER);

        if (!event.isHealthCheck()) {
            eventStateService.add(event, providerProps.getStatusTtl());
        }
    }
}
