package com.company.dragonsofmugloar.repository;

import com.company.dragonsofmugloar.domain.ad.Ad;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** In-memory copy of the last board fetched for each game, so solving an ad does not need a second fetch. */
@Repository
public class BoardRepository {

    private final Map<String, List<Ad>> boards = new ConcurrentHashMap<>();

    public void save(String gameId, List<Ad> ads) {
        boards.put(gameId, ads);
    }

    /** A finished game has no board worth keeping. */
    public void delete(String gameId) {
        boards.remove(gameId);
    }

    public Optional<Ad> findAd(String gameId, String adId) {
        return boards.getOrDefault(gameId, List.of()).stream().filter(ad -> ad.adId().equals(adId)).findFirst();
    }
}
