package no.fint.consumer.address;

import com.fasterxml.jackson.core.type.TypeReference;
import no.fint.Actions;
import no.fint.Constants;
import no.fint.dto.Address;
import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import no.fint.event.model.HeaderConstants;
import no.fint.events.FintEvents;
import no.fint.model.relation.FintResource;
import no.fint.relations.FintRelationsMediaType;
import org.redisson.api.RBlockingQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/address", produces = FintRelationsMediaType.APPLICATION_HAL_JSON_VALUE)
public class AddressController {

    private TypeReference<List<FintResource<Address>>> addressTypeReference = new TypeReference<List<FintResource<Address>>>() {
    };

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private AddressAssembler assembler;

    @GetMapping
    public ResponseEntity getAllAddresses(@RequestHeader(value = HeaderConstants.ORG_ID, defaultValue = Constants.ORGID) String orgId,
                                          @RequestHeader(value = HeaderConstants.CLIENT, defaultValue = Constants.CLIENT) String client) throws InterruptedException {
        Event<FintResource> event = new Event<>(orgId, Constants.SOURCE, Actions.GET_ALL_ADDRESSES, client);
        fintEvents.sendDownstream(orgId, event);

        RBlockingQueue<Event<FintResource>> tempQueue = fintEvents.getTempQueue("test-consumer-" + event.getCorrId());
        Event<FintResource> receivedEvent = tempQueue.poll(30, TimeUnit.SECONDS);
        List<FintResource<Address>> fintResources = EventUtil.convertEventData(receivedEvent, addressTypeReference);

        return assembler.resources(fintResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getAddress(@PathVariable String id,
                                     @RequestHeader(value = HeaderConstants.ORG_ID, defaultValue = Constants.ORGID) String orgId,
                                     @RequestHeader(value = HeaderConstants.CLIENT, defaultValue = Constants.CLIENT) String client) throws InterruptedException {
        Event<String> event = new Event<>(orgId, Constants.SOURCE, Actions.GET_ADDRESS, client);
        event.setQuery(id);
        fintEvents.sendDownstream(orgId, event);

        RBlockingQueue<Event<FintResource>> tempQueue = fintEvents.getTempQueue("test-consumer-" + event.getCorrId());
        Event<FintResource> receivedEvent = tempQueue.poll(30, TimeUnit.SECONDS);
        List<FintResource<Address>> fintResources = EventUtil.convertEventData(receivedEvent, addressTypeReference);

        return assembler.resource(fintResources.get(0));
    }
}