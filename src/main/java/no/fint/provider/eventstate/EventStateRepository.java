package no.fint.provider.eventstate;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class EventStateRepository {

    @Value("${fint.provider.eventstate.hashmap-name:event-state}")
    private String key;

    @Autowired
    private RedisTemplate<String, EventState> redisTemplate;

    private HashOperations<String, String, EventState> hashOps;

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
    }

    public boolean exists(String corrId) {
        return hashOps.hasKey(key, corrId);
    }

    public boolean exists(String corrId, Status status) {
        boolean exists = hashOps.hasKey(key, corrId);
        if (!exists) {
            return false;
        }

        EventState eventState = hashOps.get(key, corrId);
        return (eventState.getEvent().getStatus() == status);
    }

    public void add(EventState eventState) {
        hashOps.put(key, eventState.getEvent().getCorrId(), eventState);
    }

    public Optional<EventState> get(String corrId) {
        if (exists(corrId)) {
            return Optional.ofNullable(hashOps.get(key, corrId));
        }
        return Optional.empty();
    }

    public void remove(String corrId) {
        hashOps.delete(key, corrId);
    }

    public Map<String, EventState> getMap() {
        return hashOps.entries(key);
    }


}