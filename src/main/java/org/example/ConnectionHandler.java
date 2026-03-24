package org.example;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.StringReader;
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
    public static final Gson GSON = new Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().setStrictness(Strictness.LENIENT).create();
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
            try {
                var client = newHttpClient();
                connectionStatusMessage = ConnectionStatus.CONNECTING;
                CompletableFuture<WebSocket> wsFuture = client.newWebSocketBuilder().buildAsync(new URI("ws://localhost:8080/connect"), new WebSocket.Listener() {
                    final StringBuilder currentMessage = new StringBuilder();

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        currentMessage.append(data);
                        if (last) {
                            try (var reader = new JsonReader(new StringReader(currentMessage.toString()))) {
                                reader.setLenient(true);
                                while (reader.peek() != JsonToken.END_DOCUMENT) {
                                    JsonElement element = JsonParser.parseReader(reader);
                                    if (element.isJsonObject()) {
                                        Map<String, Object> messageMap = GSON.fromJson(element, new TypeToken<Map<String, Object>>() {
                                        }.getType());
                                        observers.removeIf(observer -> !observer.observing());
                                        notifyObservers(messageMap);
                                    } else {
                                        System.err.println("Received non-object JSON: " + element);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing WebSocket message: " + e.getMessage());
                            } finally {
                                currentMessage.setLength(0);
                            }
                        }
                        webSocket.request(1);
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        connectionStatusMessage = ConnectionStatus.SUCCESS;
                        System.out.println("Connected to server.");
                        webSocket.request(1);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.err.println("WebSocket error: " + error.getMessage());
                        reconnect();
                        connectionStatusMessage = ConnectionStatus.FAILED;
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        System.out.println("WebSocket closed: " + reason);
                        connectionStatusMessage = ConnectionStatus.FAILED;
                        reconnect();
                        return CompletableFuture.completedFuture(null);
                    }
                });

                var ws = wsFuture.get();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> ws.sendText("{\"request_type\": \"sign-out\"}", true)));
                connected = true;
                while (!ws.isInputClosed()) {
                    var message = sendQueue.poll();
                    if (message != null) {
                        ws.sendText(message, true);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (URISyntaxException | InterruptedException | ExecutionException e) {
                connectionStatusMessage = ConnectionStatus.FAILED;
                System.err.println("Connection failed, retrying in " + POLL.getSeconds() + " seconds...");
                try {
                    Thread.sleep(POLL.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }

    private void reconnect() {
        if (Main.getUsername() != null) {
            sendQueue.add(GSON.toJson(Map.of("request_type", "new-player", "data", Map.of("username", Main.getUsername()))));
        }
        run();
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

    public void addObserver(MessageObserver observer) {
        observers.add(observer);
    }

    public record PlayerUpdateInfo(@Expose int x, @Expose int y, @Expose List<Projectile> projectiles,
                                   @Expose List<Block> blocks, @Expose double health, @Expose double shooterAngle, @Expose boolean facingLeft) {
    }

    public enum ConnectionStatus {
        CONNECTING, FAILED, SUCCESS;

        public String toString() {
            return switch (this) {
                case CONNECTING -> "Connecting...";
                case FAILED -> "Failed";
                case SUCCESS -> "Connected";
            };
        }
    }

    public interface MessageObserver {
        void handleMessage(Map<String, Object> message);

        default boolean observing() {
            return true;
        }
    }
}