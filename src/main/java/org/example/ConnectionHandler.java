package org.example;

import com.google.gson.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static java.net.http.HttpClient.newHttpClient;

public class ConnectionHandler implements Runnable {
    private final Queue<String> sendQueue;
    private final Queue<String> readQueue;
    private final Gson GSON = new Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private String connectionStatusMessage = "Connecting to server...";

    public ConnectionHandler() {
        this.sendQueue = new ConcurrentLinkedQueue<>();
        this.readQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        connect();
    }

    private void connect() {
        final Duration POLL = Duration.ofSeconds(5);
        boolean connected = false;
        while (!connected) {
            try (var client = newHttpClient()) {
                connectionStatusMessage = "Connecting to server...";
                var ws = client.newWebSocketBuilder().buildAsync(new URI("ws://localhost:8080/connect"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        return CompletableFuture.supplyAsync(() -> {
                            readQueue.add(data.toString());
                            System.out.println("New message received: " + data);
                            return data;
                        });
                    }
                }).get();
                connectionStatusMessage = "Successfully connected to server";
                connected = true;

                while (Thread.currentThread().isAlive()) {
                    if (!sendQueue.isEmpty())
                        ws.sendText(sendQueue.poll(), true);
                }
            } catch (URISyntaxException | InterruptedException | ExecutionException _) {
                connectionStatusMessage = "Failed to connect to server";
                try {
                    Thread.sleep(POLL);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendMessage(Map<String, Object> message) {
        sendQueue.add(GSON.toJson(message));
    }

    public Map<String, Object> readMessage() {
        return GSON.<Map<String, Object>>fromJson(readQueue.poll(), Map.class);
    }

    public String getConnectionStatus() {
        return connectionStatusMessage;
    }

    public record PlayerUpdateInfo(int x, int y, List<Projectile> projectiles, List<Block> blocks) {
    }
}