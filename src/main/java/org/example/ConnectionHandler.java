package org.example;

import com.google.gson.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayList;
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
    private final Gson GSON = new Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private ConnectionStatus connectionStatusMessage = ConnectionStatus.CONNECTING;
    private final List<MessageObserver> observers;

    public ConnectionHandler() {
        this.sendQueue = new ConcurrentLinkedQueue<>();
        observers = new ArrayList<>();
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
                connectionStatusMessage = ConnectionStatus.CONNECTING;
                var ws = client.newWebSocketBuilder().buildAsync(new URI("ws://localhost:8080/connect"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        return CompletableFuture.supplyAsync(() -> {
                            notifyObservers(fromJson(data));
                            return data;
                        });
                    }
                }).get();
                connectionStatusMessage = ConnectionStatus.SUCCESS;
                connected = true;

                while (Thread.currentThread().isAlive()) {
                    if (!sendQueue.isEmpty())
                        ws.sendText(sendQueue.poll(), true);
                }
            } catch (URISyntaxException | InterruptedException | ExecutionException _) {
                connectionStatusMessage = ConnectionStatus.FAILED;
                try {
                    Thread.sleep(POLL);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Map<String, Object> fromJson(CharSequence data) {
        return  GSON.<Map<String, Object>>fromJson(data.toString(), Map.class);
    }

    private void notifyObservers(Map<String, Object> message) {
        for (var observer : observers) {
            observer.handleMessage(message);
        }
    }

    public void sendMessage(Map<String, Object> message) {
        sendQueue.add(GSON.toJson(message));
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatusMessage;
    }

    public record PlayerUpdateInfo(int x, int y, List<Projectile> projectiles, List<Block> blocks) {
    }

    public enum ConnectionStatus {
        CONNECTING, FAILED, SUCCESS;

        public String getMessage() {
            return switch (this) {
                case CONNECTING -> "Connecting to server...";
                case FAILED -> "Successfully connected to server.";
                case SUCCESS -> "Failed to connect to server.";
            };
        }
    }

    public void addObserver(MessageObserver observer) {
        observers.add(observer);
    }

    public interface MessageObserver {
        void handleMessage(Map<String, Object> message);
    }
}