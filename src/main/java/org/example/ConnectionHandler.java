package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionHandler implements Runnable {
    private final Queue<String> sendQueue;
    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setLenient().create();
    private volatile ConnectionStatus connectionStatusMessage = ConnectionStatus.CONNECTING;
    private final List<MessageObserver> observers;
    private boolean shutdownHookRegistered;

    public ConnectionHandler() {
        this.sendQueue = new ConcurrentLinkedQueue<String>();
        this.observers = new ArrayList<MessageObserver>();
    }

    @Override
    public void run() {
        connect();
    }

    private void connect() {
        final Duration poll = Duration.ofSeconds(5);

        while (true) {
            try {
                connectionStatusMessage = ConnectionStatus.CONNECTING;
                final WebSocketClient ws = new WebSocketClient(new URI("ws://localhost:8080/connect")) {
                    @Override
                    public void onOpen(ServerHandshake handshakeData) {
                        connectionStatusMessage = ConnectionStatus.SUCCESS;
                        System.out.println("Connected to server.");
                        if (Main.getUsername() != null) {
                            sendQueue.add(GSON.toJson(Maps.of(
                                    "request_type", "new-player",
                                    "data", Maps.of("username", Main.getUsername())
                            )));
                        }
                    }

                    @Override
                    public void onMessage(String message) {
                        parseMessage(message);
                    }

                    @Override
                    public void onClose(int statusCode, String reason, boolean remote) {
                        System.out.println("WebSocket closed: " + reason);
                        connectionStatusMessage = ConnectionStatus.FAILED;
                    }

                    @Override
                    public void onError(Exception error) {
                        System.err.println("WebSocket error: " + error.getMessage());
                        connectionStatusMessage = ConnectionStatus.FAILED;
                    }
                };

                ws.connectBlocking();
                registerShutdownHook(ws);
                while (ws.isOpen()) {
                    String message = sendQueue.poll();
                    if (message != null) {
                        ws.send(message);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (Exception e) {
                connectionStatusMessage = ConnectionStatus.FAILED;
                System.err.println("Connection failed, retrying in " + poll.getSeconds() + " seconds...");
                try {
                    Thread.sleep(poll.toMillis());
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(interruptedException);
                }
            }
        }
    }

    private synchronized void registerShutdownHook(final WebSocketClient ws) {
        if (shutdownHookRegistered) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (ws.isOpen()) {
                    ws.send("{\"request_type\": \"sign-out\"}");
                }
            }
        }));
        shutdownHookRegistered = true;
    }

    private void parseMessage(String message) {
        try {
            JsonReader reader = new JsonReader(new StringReader(message));
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
            reader.close();
        } catch (Exception e) {
            System.err.println("Error parsing WebSocket message: " + e.getMessage());
        }
    }

    private void notifyObservers(Map<String, Object> message) {
        for (MessageObserver observer : observers) {
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

    public static final class PlayerUpdateInfo {
        @Expose
        private final int x;
        @Expose
        private final int y;
        @Expose
        private final List<Projectile> projectiles;
        @Expose
        private final List<Block> blocks;
        @Expose
        private final double health;
        @Expose
        private final double shooterAngle;
        @Expose
        private final boolean facingLeft;

        public PlayerUpdateInfo(int x, int y, List<Projectile> projectiles, List<Block> blocks, double health, double shooterAngle, boolean facingLeft) {
            this.x = x;
            this.y = y;
            this.projectiles = projectiles;
            this.blocks = blocks;
            this.health = health;
            this.shooterAngle = shooterAngle;
            this.facingLeft = facingLeft;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public List<Projectile> projectiles() {
            return projectiles;
        }

        public List<Block> blocks() {
            return blocks;
        }

        public double health() {
            return health;
        }

        public double shooterAngle() {
            return shooterAngle;
        }

        public boolean facingLeft() {
            return facingLeft;
        }
    }

    public enum ConnectionStatus {
        CONNECTING, FAILED, SUCCESS;

        public String toString() {
            switch (this) {
                case CONNECTING:
                    return "Connecting...";
                case FAILED:
                    return "Failed";
                case SUCCESS:
                    return "Connected";
                default:
                    throw new IllegalStateException("Unhandled connection status: " + this);
            }
        }
    }

    public interface MessageObserver {
        void handleMessage(Map<String, Object> message);

        default boolean observing() {
            return true;
        }
    }
}
