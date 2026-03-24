# duel.io Java Client

This repository contains the Java 8 game client for `duel.io`.

It is the source of truth for the client codebase, including:

- the desktop Swing client
- shared gameplay logic and rendering
- network message handling
- browser-compatible bridge classes used by the web-hosted build

The web hosting layer itself does not live here anymore. The browser-served version is hosted by the Go server repository, while this repository remains focused on the Java client and its Maven build.

## Requirements

- Java 8 compatible JDK
- Maven

## Build

Build the client with Maven:

```bash
mvn clean package
```

This produces a shaded runnable jar in `target/`, including dependencies declared in [pom.xml](pom.xml).

Common output:

- `target/duel.io-0.7.jar`

## Run

Run the desktop client:

```bash
java -jar target/duel.io-0.7.jar
```

The desktop client expects the multiplayer server to be available at:

```text
ws://localhost:8080/connect
```

## Project Structure

- [src/main/java/org/example/Main.java](src/main/java/org/example/Main.java): desktop entry point and window setup
- [src/main/java/org/example/GamePanel.java](src/main/java/org/example/GamePanel.java): main game loop, rendering, and scene state
- [src/main/java/org/example/Player.java](src/main/java/org/example/Player.java): local player state and controls
- [src/main/java/org/example/Opponent.java](src/main/java/org/example/Opponent.java): remote player representation
- [src/main/java/org/example/PhysicsHandler.java](src/main/java/org/example/PhysicsHandler.java): collisions, movement, and world interaction
- [src/main/java/org/example/ConnectionHandler.java](src/main/java/org/example/ConnectionHandler.java): WebSocket networking, reconnection, and message dispatch
- [src/main/java/org/example/SidePanel.java](src/main/java/org/example/SidePanel.java): connection state and player list UI
- [src/main/java/org/example/BrowserWebSocketBridge.java](src/main/java/org/example/BrowserWebSocketBridge.java): native bridge used when the client is run inside the browser

## Dependencies

The client currently uses:

- Gson for JSON serialization/deserialization
- Java-WebSocket for desktop WebSocket connectivity
- `slf4j-nop` to suppress logging backend requirements from dependencies

These dependencies are managed by Maven in [pom.xml](pom.xml).

## Browser Build Context

This repository still contains the browser bridge classes needed for the web version, but it does not serve the site itself.

Current split of responsibilities:

- this repo: builds and owns the Java client code
- Go server repo: serves the browser page, bundled jars, static assets, and `/connect`

## Notes

- The codebase has been migrated to Java 8-compatible syntax and APIs.
- `dependency-reduced-pom.xml` is a generated Maven artifact from shading and can appear after packaging.
- Build outputs in `target/` are generated artifacts, not source files.
