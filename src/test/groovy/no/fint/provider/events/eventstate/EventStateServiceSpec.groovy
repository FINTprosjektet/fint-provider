package no.fint.provider.events.eventstate

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import no.fint.event.model.Event
import no.fint.provider.events.ProviderProps
import spock.lang.Specification

class EventStateServiceSpec extends Specification {
    private EventStateService eventStateService
    private HazelcastInstance hazelcastInstance

    void setup() {
        def props = Mock(ProviderProps) {
            getKey() >> 'current-corrids'
        }
        hazelcastInstance = Mock(HazelcastInstance)
        eventStateService = new EventStateService(eventStates: [:], providerProps: props, hazelcastInstance: hazelcastInstance)
    }

    def "Init EventStateService"() {
        when:
        eventStateService.init()

        then:
        1 * hazelcastInstance.getMap('current-corrids') >> Mock(IMap)
    }

    def "Add and get new EventState"() {
        given:
        def event = new Event('rogfk.no', 'test', 'GET_ALL', 'test')

        when:
        eventStateService.add(event, 2)
        def eventState = eventStateService.get(event)

        then:
        eventState.event == event
        eventStateService.getEventStates().size() == 1
    }

    def "Remove existing EventState"() {
        given:
        def event = new Event('rogfk.no', 'test', 'GET_ALL', 'test')
        eventStateService.add(event, 2)

        when:
        eventStateService.remove(event)
        def eventState = eventStateService.get(event)

        then:
        !eventState
    }

    def "Trying to remove EventState that does not exist"() {
        given:
        def event = new Event()

        when:
        eventStateService.remove(event)
        def eventState = eventStateService.get(event)

        then:
        !eventState
    }

    def "Get expired EventStates"() {
        given:
        def event1 = new Event('rogfk.no', 'test', 'GET_ALL', 'test')
        def event2 = new Event('rogfk.no', 'test', 'GET_ALL', 'test')
        eventStateService.add(event1, -2)
        eventStateService.add(event2, 2)

        when:
        def expired = eventStateService.getExpiredEvents()

        then:
        expired.size() == 1
        eventStateService.getEventStates().size() == 1
    }
}
