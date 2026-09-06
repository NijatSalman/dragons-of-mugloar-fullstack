package com.company.dragonsofmugloar.repository;

import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSession;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Repository;

/** In-memory store of autoplay sessions. Updates are atomic per session, so parallel games can report safely. */
@Repository
public class AutoplayGameSessionRepository {

    private final Map<String, AutoplayGameSession> sessions = new ConcurrentHashMap<>();

    public void save(AutoplayGameSession session) {
        sessions.put(session.sessionId(), session);
    }

    public Optional<AutoplayGameSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void update(String sessionId, UnaryOperator<AutoplayGameSession> change) {
        sessions.computeIfPresent(sessionId, (id, session) -> change.apply(session));
    }
}
