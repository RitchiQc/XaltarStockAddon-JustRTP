package com.ritchiqc.justrtpaddon.data;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {

    void connect();

    void disconnect();

    CompletableFuture<Integer> getStock(UUID playerId);

    CompletableFuture<Void> setStock(UUID playerId, int amount);

    CompletableFuture<Boolean> hasFirstJoined(UUID playerId);

    CompletableFuture<Void> setFirstJoined(UUID playerId);
}
